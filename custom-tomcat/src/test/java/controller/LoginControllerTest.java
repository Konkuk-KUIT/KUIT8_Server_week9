package controller;

import db.MemoryUserRepository;
import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {
    private final Repository repository = MemoryUserRepository.getInstance();
    private final LoginController controller = new LoginController(repository);

    private HttpRequest loginRequest(String body) throws IOException {
        String raw = "POST /user/login HTTP/1.1\r\n"
                + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                + "\r\n" + body;
        return HttpRequest.from(new BufferedReader(new StringReader(raw)));
    }

    private String execute(String body) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        controller.execute(loginRequest(body), new HttpResponse(out));
        return out.toString();
    }

    @Test
    void 로그인_성공하면_쿠키를_담아_index로_302() throws IOException {
        repository.addUser(new User("login-ok", "pw", "성공", "a@test.com"));

        String res = execute("userId=login-ok&password=pw");

        assertTrue(res.startsWith("HTTP/1.1 302 Found"));
        assertTrue(res.contains("Location: /index.html"));
        assertTrue(res.contains("Set-Cookie: logined=true; Path=/"));
    }

    @Test
    void 비밀번호가_틀리면_쿠키_없이_실패_화면으로_302() throws IOException {
        repository.addUser(new User("login-bad", "pw", "실패", "b@test.com"));

        String res = execute("userId=login-bad&password=wrong");

        assertTrue(res.contains("Location: /user/login_failed.html"));
        assertFalse(res.contains("Set-Cookie"));
    }

    @Test
    void 가입하지_않은_아이디도_예외_없이_실패_화면으로_302() throws IOException {
        String res = execute("userId=nobody-here&password=pw");

        assertTrue(res.contains("Location: /user/login_failed.html"));
    }
}