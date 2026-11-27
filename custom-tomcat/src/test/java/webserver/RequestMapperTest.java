package webserver;

import controller.HomeController;
import controller.ListController;
import controller.LoginController;
import controller.SignUpController;
import controller.StaticFileController;
import db.MemoryUserRepository;
import http.HttpMethod;
import http.HttpRequest;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {
    private final RequestMapper mapper = new RequestMapper(Map.of(
            new RequestKey(HttpMethod.GET, "/"), new HomeController(),
            new RequestKey(HttpMethod.GET, "/user/userList"), new ListController(),
            new RequestKey(HttpMethod.GET, "/user/list.html"), new ListController(),
            new RequestKey(HttpMethod.POST, "/user/signup"), new SignUpController(MemoryUserRepository.getInstance()),
            new RequestKey(HttpMethod.POST, "/user/login"), new LoginController(MemoryUserRepository.getInstance())
    ), new StaticFileController());

    @Test
    void selectsRegisteredPostControllers() throws IOException {
        assertInstanceOf(SignUpController.class, mapper.getController(request("POST", "/user/signup")));
        assertInstanceOf(LoginController.class, mapper.getController(request("POST", "/user/login")));
    }

    @Test
    void selectsHomeAndProtectedListRoutes() throws IOException {
        assertInstanceOf(HomeController.class, mapper.getController(request("GET", "/")));
        assertInstanceOf(ListController.class, mapper.getController(request("GET", "/user/userList")));
        assertInstanceOf(ListController.class, mapper.getController(request("GET", "/user/list.html")));
    }

    @Test
    void samePathWithDifferentMethodsDoesNotSelectTheSameController() throws IOException {
        assertInstanceOf(SignUpController.class, mapper.getController(request("POST", "/user/signup")));
        assertInstanceOf(StaticFileController.class, mapper.getController(request("GET", "/user/signup")));
        assertInstanceOf(StaticFileController.class, mapper.getController(request("POST", "/user/list.html")));
    }

    @Test
    void queryStringDoesNotChangeControllerSelection() throws IOException {
        assertInstanceOf(HomeController.class, mapper.getController(request("GET", "/?source=menu")));
        assertInstanceOf(ListController.class, mapper.getController(request("GET", "/user/list.html?page=2")));
        assertInstanceOf(SignUpController.class, mapper.getController(request("POST", "/user/signup?source=form")));
    }

    @Test
    void unmappedRequestsUseStaticControllerForFileAndNotFoundHandling() throws IOException {
        assertInstanceOf(StaticFileController.class, mapper.getController(request("GET", "/index.html")));
        assertInstanceOf(StaticFileController.class, mapper.getController(request("GET", "/css/styles.css")));
        assertInstanceOf(StaticFileController.class, mapper.getController(request("GET", "/unknown")));
        assertInstanceOf(StaticFileController.class, mapper.getController(request("POST", "/css/styles.css")));
        assertInstanceOf(StaticFileController.class, mapper.getController(request("POST", "/unknown")));
    }

    private static HttpRequest request(String method, String target) throws IOException {
        return HttpRequest.from(new BufferedReader(new StringReader(
                method + " " + target + " HTTP/1.1\r\n\r\n")));
    }
}
