
package webserver;

import controller.Controller;
import controller.ListController;
import controller.LoginController;
import controller.StaticFileController;
import db.MemoryUserRepository;
import db.Repository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {

    private HttpRequest createRequest(String rawRequest) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(rawRequest));
        return HttpRequest.from(reader);
    }

    private String dispatch(RequestMapper mapper, String rawRequest) throws Exception {
        HttpRequest request = createRequest(rawRequest);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output);

        mapper.dispatch(request, response);

        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void postLoginReturnsLoginController() {
        Repository repository = MemoryUserRepository.getInstance();

        LoginController loginController = new LoginController(repository);
        RequestMapper mapper = new RequestMapper(new StaticFileController());

        mapper.register(HttpMethod.POST, "/user/login", loginController);

        Controller result = mapper.findController(HttpMethod.POST, "/user/login");

        assertSame(loginController, result);
        assertNull(mapper.findController(HttpMethod.GET, "/user/login"));
    }

    @Test
    void unmappedGetRequestServesStaticFile() throws Exception {
        RequestMapper mapper = new RequestMapper(new StaticFileController());

        String result = dispatch(mapper,
                "GET /css/styles.css HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(result.contains("200"), result);
    }

    @Test
    void nonexistentFileReturns404() throws Exception {
        RequestMapper mapper = new RequestMapper(new StaticFileController());

        String result = dispatch(mapper,
                "GET /not-found-test.html HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(result.contains("404"), result);
    }

    @Test
    void unauthenticatedUserListRedirectsToLogin() throws Exception {
        RequestMapper mapper = new RequestMapper(new StaticFileController());

        mapper.register(HttpMethod.GET, "/user/list.html", new ListController());

        String result = dispatch(mapper,
                "GET /user/list.html HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(result.contains("302"), result);
        assertTrue(result.contains("/user/login.html"), result);
    }
}
