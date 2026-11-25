package webserver.controller;

import db.MemoryUserRepository;
import db.Repository;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import webserver.HttpMethod;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {
    private Repository repo;
    private Controller controller;
    private ByteArrayOutputStream out;
    private HttpResponse res;

    @BeforeEach
    void setUp() {
        repo = new MemoryUserRepository();
        repo.addUser(new User("user", "user", "user", "user@user.net"));  // 본인 메서드/생성자에 맞게
        controller = new LoginController(repo);
        out = new ByteArrayOutputStream();
        res = new HttpResponse(new DataOutputStream(out));
    }

    @Test
    public void 로그인_성공() throws IOException {
        controller.execute(loginRequest("userId=user&password=user"), res);

        String output = out.toString();
        assertThat(output).contains("302");
        assertThat(output).contains("Location: /index.html");
    }

    private HttpRequest loginRequest(String body) {
        return new HttpRequest(
                HttpMethod.POST,
                body.getBytes(StandardCharsets.UTF_8).length,
                false,
                "/user/login",
                Map.of(),
                body
        );
    }
}