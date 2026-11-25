package webserver;

import db.MemoryUserRepository;
import db.Repository;
import model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import webserver.controller.Controller;
import webserver.controller.HomeController;
import webserver.controller.LoginController;
import webserver.controller.StaticFileController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {

    private Repository repo;
    private RequestMapper requestMapper;

    @BeforeEach
    void setting() {
        repo = new MemoryUserRepository();
        repo.addUser(new User("user1", "user1", "user1", "user1@user1.user1"));

        requestMapper = new RequestMapper(repo);
    }

    @Test
    public void 컨트롤러가_있을때_잘_매핑되는지(){

        Controller loginController = requestMapper.mapping(LoginRequest());

        Assertions.assertThat(loginController).isInstanceOf(LoginController.class);
    }


    @Test
    public void 컨트롤러가_없을때_정적_리소스_반환(){

        Controller staticController = requestMapper.mapping(StaticFileRequest());

        Assertions.assertThat(staticController).isInstanceOf(StaticFileController.class);
    }


    private HttpRequest LoginRequest() {
        return new HttpRequest(
                HttpMethod.POST,
                27,
                false,
                "/user/login",
                Map.of(),
                "userId=user1&password=user1"
        );
    }

    private HttpRequest StaticFileRequest() {
        return new HttpRequest(
                HttpMethod.GET,
                0,
                false,
                "/user/login.html",
                Map.of(),
                ""
        );
    }
}