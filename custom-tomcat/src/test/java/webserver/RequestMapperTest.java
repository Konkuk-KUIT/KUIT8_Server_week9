package webserver;

import controller.*;
import db.MemoryUserRepository;
import db.Repository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {
    private final Repository repository = MemoryUserRepository.getInstance();
    private final LoginController loginController = new LoginController(repository);
    private final ListController listController = new ListController();
    private final HomeController homeController = new HomeController();
    private final RequestMapper mapper = new RequestMapper(Map.of(
            new RequestKey(HttpMethod.GET, "/"), homeController,
            new RequestKey(HttpMethod.GET, "/index.html"), homeController,
            new RequestKey(HttpMethod.POST, "/user/signup"), new SignUpController(repository),
            new RequestKey(HttpMethod.POST, "/user/login"), loginController,
            new RequestKey(HttpMethod.GET, "/user/userList"), listController,
            new RequestKey(HttpMethod.GET, "/user/list.html"), listController
    ), new StaticFileController(), new NotFoundController());

    private HttpRequest request(String method, String path, String headers, String body) throws Exception {
        String raw = method + " " + path + " HTTP/1.1\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + headers + "\r\n" + body;
        return HttpRequest.from(new BufferedReader(new StringReader(raw)));
    }

    private String dispatch(String method, String path, String headers, String body) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.dispatch(request(method, path, headers, body), new HttpResponse(new DataOutputStream(out)));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void selectsRegisteredControllerWithoutQueryString() throws Exception {
        assertSame(loginController, mapper.findController(request("POST", "/user/login?from=home", "", "")));
        assertSame(homeController, mapper.findController(request("GET", "/", "", "")));
    }

    @Test
    void respondsWithUnmappedStaticFile() throws Exception {
        String response = dispatch("GET", "/css/styles.css", "", "");
        assertTrue(response.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(response.contains("Content-Type: text/css\r\n"));
        assertTrue(response.endsWith(Files.readString(Path.of("webapp/css/styles.css"))));
    }

    @Test
    void respondsWithNotFoundForMissingPath() throws Exception {
        assertTrue(dispatch("GET", "/missing", "", "").startsWith("HTTP/1.1 404 Not Found\r\n"));
    }

    @Test
    void doesNotRouteDifferentMethodToRegisteredControllerOrStaticFile() throws Exception {
        assertTrue(dispatch("GET", "/user/login", "", "").startsWith("HTTP/1.1 404"));
        assertTrue(dispatch("POST", "/index.html", "", "").startsWith("HTTP/1.1 404"));
    }

    @Test
    void redirectsBothListPathsWithoutLoginCookie() throws Exception {
        for (String path : new String[]{"/user/list.html", "/user/userList"}) {
            String response = dispatch("GET", path, "", "");
            assertTrue(response.startsWith("HTTP/1.1 302 Found\r\n"));
            assertTrue(response.contains("Location: /user/login.html\r\n"));
        }
    }

    @Test
    void returnsListWithLoginCookie() throws Exception {
        String response = dispatch("GET", "/user/list.html", "Cookie: logined=true\r\n", "");
        assertTrue(response.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(response.endsWith(Files.readString(Path.of("webapp/user/list.html"))));
    }

    @Test
    void signsUpAndLogsInUsingInjectedRepository() throws Exception {
        String signup = dispatch("POST", "/user/signup", "", "userId=mapper-user&password=secret&name=Mapper&email=a%40b.com");
        assertTrue(signup.contains("Location: /index.html\r\n"));
        assertEquals(new User("mapper-user", "secret", "Mapper", "a@b.com"), repository.findUserById("mapper-user"));
        String login = dispatch("POST", "/user/login", "", "userId=mapper-user&password=secret");
        assertTrue(login.contains("Location: /index.html\r\n"));
        assertTrue(login.contains("Set-Cookie: logined=true; Path=/\r\n"));
        String failed = dispatch("POST", "/user/login", "", "userId=mapper-user&password=wrong");
        assertTrue(failed.contains("Location: /user/login_failed.html\r\n"));
        assertFalse(failed.contains("Set-Cookie:"));
    }

    @Test
    void doesNotServeFilesOutsideWebRoot() throws Exception {
        assertTrue(dispatch("GET", "/../build.gradle", "", "").startsWith("HTTP/1.1 404"));
    }
}
