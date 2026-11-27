package webserver;

import controller.ListController;
import controller.LoginController;
import controller.SignUpController;
import controller.StaticFileController;
import db.MemoryUserRepository;
import db.Repository;
import http.HttpMethod;
import http.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {

    private RequestMapper requestMapper;
    private LoginController loginController;
    private StaticFileController staticFileController;

    @BeforeEach
    void setUp() {
        Repository repository = MemoryUserRepository.getInstance();

        loginController = new LoginController(repository);
        staticFileController = new StaticFileController();

        SignUpController signUpController = new SignUpController(repository);
        ListController listController = new ListController();

        requestMapper = new RequestMapper();

        requestMapper.register(HttpMethod.POST, "/user/signup", signUpController);

        requestMapper.register(HttpMethod.POST, "/user/login", loginController);

        requestMapper.register(HttpMethod.GET, "/user/userList", listController);

        requestMapper.register(HttpMethod.GET, "/user/list.html", listController);
    }

    // 1. 로그인 요청에 LoginController가 선택되는지 확인
    @Test
    void loginRequestSelectsLoginController() throws IOException {
        String rawRequest = "POST /user/login HTTP/1.1\r\n" + "Content-Length: 0\r\n\r\n";

        HttpRequest request = HttpRequest.from(new BufferedReader(new StringReader(rawRequest)));

        assertSame(loginController, requestMapper.findController(request));
    }

    // 2. 매핑에 없는 CSS 파일 요청이 정상 응답되는지 확인
    @Test
    void staticCssFileReturns200() {
        String response = send("GET /css/styles.css HTTP/1.1\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 200"));
        assertTrue(response.contains("Content-Type: text/css"));
        assertFalse(response.split("\r\n\r\n", 2)[1].isBlank());
    }

    // 3. 존재하지 않는 경로 요청에 404가 반환되는지 확인
    @Test
    void unknownPathReturns404() {
        String response = send("GET /not-existing-page HTTP/1.1\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 404"));
    }

    // 4. 쿠키 없이 회원 목록에 접근하면 로그인 페이지로 이동
    @Test
    void userListWithoutCookieRedirectsToLogin() {
        String response = send("GET /user/list.html HTTP/1.1\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 302"));
        assertTrue(response.contains("Location: /user/login.html\r\n"));
    }

    private String send(String request) {
        TestSocket socket = new TestSocket(request);

        new RequestHandler(socket, requestMapper, staticFileController).run();

        return socket.output.toString(StandardCharsets.UTF_8);
    }

    private static class TestSocket extends Socket {

        private final InputStream input;

        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        TestSocket(String request) {
            input = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public InputStream getInputStream() {
            return input;
        }

        @Override
        public OutputStream getOutputStream() {
            return output;
        }
    }
}