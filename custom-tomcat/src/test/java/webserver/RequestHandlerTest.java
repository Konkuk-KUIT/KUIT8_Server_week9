package webserver;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class RequestHandlerTest {
    private String send(String request) {
        TestSocket socket = new TestSocket(request);
        new RequestHandler(socket).run();
        return socket.output.toString(StandardCharsets.UTF_8);
    }

    private String post(String path, String body) {
        return send("POST " + path + " HTTP/1.1\r\nContent-Length: "
                + body.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + body);
    }

    @Test
    void signupAndLoginSuccessAndFailures() {
        String signup = post("/user/signup",
                "userId=test&password=pass&name=Kim&email=kim%40example.com");
        assertTrue(signup.contains("Location: /index.html\r\n"));

        String success = post("/user/login", "userId=test&password=pass");
        assertTrue(success.startsWith("HTTP/1.1 302"));
        assertTrue(success.contains("Location: /index.html\r\n"));
        assertTrue(success.contains("Set-Cookie: logined=true; Path=/\r\n"));

        String wrongPassword = post("/user/login", "userId=test&password=wrong");
        String unknownUser = post("/user/login", "userId=unknown-test-user&password=pass");
        assertTrue(wrongPassword.contains("Location: /user/login_failed.html\r\n"));
        assertTrue(unknownUser.contains("Location: /user/login_failed.html\r\n"));
        assertFalse(wrongPassword.contains("Set-Cookie:"));
    }

    @Test
    void protectsBothListUrlsAndAllowsLoggedInUser() {
        for (String path : new String[]{"/user/userList", "/user/list.html"}) {
            String denied = send("GET " + path + " HTTP/1.1\r\n\r\n");
            assertTrue(denied.contains("Location: /user/login.html\r\n"));
            String allowed = send("GET " + path + " HTTP/1.1\r\nCookie: x=1; logined=true\r\n\r\n");
            assertTrue(allowed.startsWith("HTTP/1.1 200 OK\r\n"));
            assertTrue(allowed.contains("Content-Type: text/html"));
        }
    }

    private static class TestSocket extends Socket {
        // 실제 네트워크 연결 없이 요청을 넣고 응답을 확인한다.
        private final InputStream input;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        TestSocket(String request) {
            input = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        }

        @Override public InputStream getInputStream() { return input; }
        @Override public OutputStream getOutputStream() { return output; }
    }
}
