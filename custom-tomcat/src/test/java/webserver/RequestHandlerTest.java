package webserver;

import db.MemoryUserRepository;
import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import support.HttpTestResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class RequestHandlerTest {
    @Test
    void postSignupAndLoginAllowBothListRoutes() throws Exception {
        String id = UUID.randomUUID().toString();
        String password = "p+123=ok";
        String name = "홍 길동";
        String email = "student+test@example.com";
        String form = "userId=" + id + "&password=" + encode(password)
                + "&name=" + encode(name) + "&email=" + encode(email);

        HttpTestResponse signup = post("/user/signup", form);
        assertRedirect(signup, "/index.html");
        assertNull(signup.header("Set-Cookie"));
        User saved = MemoryUserRepository.getInstance().findUserById(id);
        assertNotNull(saved);
        assertEquals(password, saved.getPassword());
        assertEquals(name, saved.getName());
        assertEquals(email, saved.getEmail());

        HttpTestResponse login = post("/user/login", "userId=" + id + "&password=" + encode(password));
        assertRedirect(login, "/index.html");
        assertEquals("logined=true; Path=/", login.header("Set-Cookie"));
        // 브라우저도 Set-Cookie를 보관한 뒤 이름=값 부분을 Cookie로 보냅니다.
        String cookie = login.header("Set-Cookie").split(";", 2)[0];
        for (String route : new String[]{"/user/userList", "/user/list.html"}) {
            assertList(get(route, cookie));
        }
    }

    @Test
    void getSignupStillStoresAUserAndRedirects() throws Exception {
        String id = UUID.randomUUID().toString();
        HttpTestResponse response = get("/user/signup?userId=" + id
                + "&password=pass&name=Sample+User&email=sample%40example.com", null);

        assertRedirect(response, "/index.html");
        User saved = MemoryUserRepository.getInstance().findUserById(id);
        assertNotNull(saved);
        assertEquals("Sample User", saved.getName());
        assertEquals("sample@example.com", saved.getEmail());
    }

    @Test
    void rejectsAWrongPasswordWithoutIssuingALoginCookie() throws Exception {
        String id = UUID.randomUUID().toString();
        MemoryUserRepository.getInstance().addUser(new User(id, "correct", "Sample", "sample@example.com"));

        HttpTestResponse response = post("/user/login", "userId=" + id + "&password=wrong");

        assertRedirect(response, "/user/login_failed.html");
        assertNull(response.header("Set-Cookie"));
    }

    @Test
    void rejectsAnUnknownUserWithoutIssuingALoginCookie() throws Exception {
        HttpTestResponse response = post("/user/login", "userId=" + UUID.randomUUID() + "&password=pass");

        assertRedirect(response, "/user/login_failed.html");
        assertNull(response.header("Set-Cookie"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"userId=student", "password=pass", ""})
    void rejectsMissingLoginFields(String form) throws Exception {
        HttpTestResponse response = post("/user/login", form);

        assertRedirect(response, "/user/login_failed.html");
        assertNull(response.header("Set-Cookie"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/user/userList", "/user/list.html"})
    void redirectsAnonymousListRequestsToLogin(String path) throws Exception {
        assertRedirect(get(path, null), "/user/login.html");
    }

    @ParameterizedTest
    @ValueSource(strings = {"logined=false", "notlogined=true", "token=logined=true", "logined=trueish", "logined=TRUE"})
    void rejectsCookiesThatAreNotExactlyLoginedTrue(String cookie) throws Exception {
        for (String path : new String[]{"/user/userList", "/user/list.html"}) {
            assertRedirect(get(path, cookie), "/user/login.html");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/user/userList", "/user/list.html"})
    void findsTheLoginCookieAmongOtherCookies(String path) throws Exception {
        assertList(get(path, "theme=dark; logined=true; language=ko"));
    }

    @Test
    void normalizesTheDirectListPathBeforeCheckingLogin() throws Exception {
        assertRedirect(get("/user/../user/list.html", null), "/user/login.html");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/../webapp/user/list.html", "/user/../../webapp/user/list.html"})
    void rejectsParentPathsThatCouldBypassTheListCheck(String path) throws Exception {
        assertEquals("HTTP/1.1 404 Not Found", get(path, null).statusLine());
    }

    @Test
    void closesAnIncompleteSignupWithoutSavingIt() throws Exception {
        String id = UUID.randomUUID().toString();
        String body = "userId=" + id + "&password=pass&name=Sample&email=sample%40example.com";
        String request = "POST /user/signup HTTP/1.1\r\nContent-Length: "
                + (body.length() + 5) + "\r\n\r\n" + body;

        assertEquals(0, exchange(request).length);
        assertNull(MemoryUserRepository.getInstance().findUserById(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/css/styles.css"})
    void keepsPublicHtmlAndCssAvailableWithoutLogin(String path) throws Exception {
        String relativePath = "/".equals(path) ? "index.html" : path.substring(1);
        byte[] expected = Files.readAllBytes(Path.of("webapp", relativePath));
        HttpTestResponse response = get(path, null);

        assertEquals("HTTP/1.1 200 OK", response.statusLine());
        assertEquals(path.endsWith(".css") ? "text/css" : "text/html;charset=utf-8", response.header("Content-Type"));
        assertEquals(Integer.toString(expected.length), response.header("Content-Length"));
        assertArrayEquals(expected, response.body());
    }

    private void assertList(HttpTestResponse response) throws Exception {
        byte[] expected = Files.readAllBytes(Path.of("webapp", "user", "list.html"));
        assertEquals("HTTP/1.1 200 OK", response.statusLine());
        assertEquals("text/html;charset=utf-8", response.header("Content-Type"));
        assertEquals(Integer.toString(expected.length), response.header("Content-Length"));
        assertArrayEquals(expected, response.body());
    }

    private void assertRedirect(HttpTestResponse response, String path) {
        assertEquals("HTTP/1.1 302 Found", response.statusLine());
        assertEquals(path, response.header("Location"));
        assertEquals("0", response.header("Content-Length"));
        assertEquals("close", response.header("Connection"));
        assertEquals(0, response.body().length);
    }

    private HttpTestResponse get(String path, String cookie) throws Exception {
        String request = "GET " + path + " HTTP/1.1\r\nHost: localhost\r\n";
        if (cookie != null) {
            request += "Cookie: " + cookie + "\r\n";
        }
        return HttpTestResponse.parse(exchange(request + "\r\n"));
    }

    private HttpTestResponse post(String path, String body) throws Exception {
        String request = "POST " + path + " HTTP/1.1\r\nHost: localhost\r\n"
                + "Content-Type: application/x-www-form-urlencoded\r\n"
                + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + body;
        return HttpTestResponse.parse(exchange(request));
    }

    // OS가 배정한 빈 포트를 사용하며, 요청이 끝나면 소켓과 실행 스레드를 닫습니다.
    private byte[] exchange(String request) throws Exception {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        try (ServerSocket listener = new ServerSocket(0, 1, loopback);
             var worker = Executors.newSingleThreadExecutor()) {
            listener.setSoTimeout(5000);
            var handling = worker.submit(() -> {
                try {
                    new RequestHandler(listener.accept()).run();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            byte[] response;
            try (Socket client = new Socket(loopback, listener.getLocalPort())) {
                client.setSoTimeout(5000);
                client.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
                client.getOutputStream().flush();
                // 불완전한 본문도 서버가 EOF로 판단하고 연결을 닫을 수 있게 합니다.
                client.shutdownOutput();
                response = client.getInputStream().readAllBytes();
            }
            handling.get(5, TimeUnit.SECONDS);
            return response;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
