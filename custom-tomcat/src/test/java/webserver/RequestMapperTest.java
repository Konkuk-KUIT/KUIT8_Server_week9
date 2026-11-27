package webserver;

import controller.Controller;
import controller.LoginController;
import db.Repository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import support.HttpTestResponse;
import support.TestRequestMapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {
    // 컨트롤러가 주입된 저장소를 사용하는지도 검증합니다.
    private final Repository repository = new Repository() {
        private final Map<String, User> users = new ConcurrentHashMap<>();

        public void addUser(User user) { users.put(user.getUserId(), user); }
        public User findUserById(String id) { return users.get(id); }
        public Collection<User> findAll() { return users.values(); }
    };
    private final RequestMapper mapper = TestRequestMapper.create(repository);

    @Test
    void selectsLoginControllerByMethodAndPath() {
        Controller login = mapper.getController(HttpMethod.POST, "/user/login");
        assertInstanceOf(LoginController.class, login);
        assertSame(login, mapper.getController(HttpMethod.POST, "/user/login"));
        assertNull(mapper.getController(HttpMethod.GET, "/user/login"));
        assertNull(mapper.getController(HttpMethod.UNKNOWN, "/user/login"));
    }

    @Test
    void servesUnmappedCssWithContentTypeAndFileBody() throws Exception {
        assertNull(mapper.getController(HttpMethod.GET, "/css/styles.css"));
        HttpTestResponse response = dispatch("GET", "/css/styles.css", null, "");
        assertEquals("HTTP/1.1 200 OK", response.statusLine());
        assertEquals("text/css", response.header("Content-Type"));
        assertArrayEquals(Files.readAllBytes(Path.of("webapp/css/styles.css")), response.body());
    }

    @ParameterizedTest
    @CsvSource({"/js/script.js, text/javascript;charset=utf-8", "/img/KUIT.png, image/png",
            "/img/picture.jpeg, image/jpeg"})
    void servesOtherStaticAssets(String path, String contentType) throws Exception {
        HttpTestResponse response = dispatch("GET", path, null, "");
        assertEquals("HTTP/1.1 200 OK", response.statusLine());
        assertEquals(contentType, response.header("Content-Type"));
        assertArrayEquals(Files.readAllBytes(Path.of("webapp", path.substring(1))), response.body());
    }

    @Test
    void usesTheFileSystemsPathComparisonForProtectedFiles() throws Exception {
        String path = "/USER/LIST.HTML";
        boolean sameFilePath = Path.of(path).equals(Path.of("/user/list.html"));
        if (sameFilePath) {
            assertEquals("/user/login.html", dispatch("GET", path, null, "").header("Location"));
        } else {
            assertNull(mapper.getController(HttpMethod.GET, path));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/missing", "/missing.html", "/css/missing.css", "/../webapp/index.html"})
    void respondsNotFoundWhenNeitherControllerNorFileExists(String path) throws Exception {
        assertEquals("HTTP/1.1 404 Not Found", dispatch("GET", path, null, "").statusLine());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/user/userList", "/user/list.html", "/user/../user/list.html"})
    void protectsListBeforeServingTheStaticFile(String path) throws Exception {
        HttpTestResponse anonymous = dispatch("GET", path, null, "");
        assertEquals("HTTP/1.1 302 Found", anonymous.statusLine());
        assertEquals("/user/login.html", anonymous.header("Location"));

        HttpTestResponse loggedIn = dispatch("GET", path, "logined=true", "");
        assertEquals("HTTP/1.1 200 OK", loggedIn.statusLine());
        assertArrayEquals(Files.readAllBytes(Path.of("webapp/user/list.html")), loggedIn.body());
        assertEquals("HTTP/1.1 302 Found", dispatch("GET", path, null, "").statusLine());
    }

    @Test
    void rejectsPostToAStaticFile() throws Exception {
        assertEquals("HTTP/1.1 404 Not Found", dispatch("POST", "/index.html", null, "").statusLine());
    }

    @Test
    void signupAndLoginUseTheInjectedRepositoryWithoutKeepingRequestState() throws Exception {
        HttpTestResponse signup = dispatch("POST", "/user/signup", null,
                "userId=student&password=pass&name=Student&email=student%40example.com");
        assertEquals("/index.html", signup.header("Location"));
        assertEquals("Student", repository.findUserById("student").getName());

        HttpTestResponse success = dispatch("POST", "/user/login", null, "userId=student&password=pass");
        assertEquals("/index.html", success.header("Location"));
        assertEquals("logined=true; Path=/", success.header("Set-Cookie"));

        HttpTestResponse failure = dispatch("POST", "/user/login", null, "userId=student&password=wrong");
        assertEquals("/user/login_failed.html", failure.header("Location"));
        assertNull(failure.header("Set-Cookie"));
    }

    private HttpTestResponse dispatch(String method, String path, String cookie, String body) throws Exception {
        String headers = cookie == null ? "" : "Cookie: " + cookie + "\r\n";
        String rawRequest = method + " " + path + " HTTP/1.1\r\n" + headers
                + "Content-Length: " + body.length() + "\r\n\r\n" + body;
        HttpRequest request = HttpRequest.from(new BufferedReader(new StringReader(rawRequest)));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mapper.dispatch(request, new HttpResponse(output));
        return HttpTestResponse.parse(output.toByteArray());
    }
}
