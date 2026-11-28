package webserver;

import controller.LoginController;
import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static http.HttpMethod.GET;
import static http.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestMapperTest {
    private final RequestMapper mapper = WebServer.createRequestMapper(MemoryUserRepository.getInstance());
    private final FrontController frontController = new FrontController(mapper);

    @Test
    void selectsControllerByMethodAndPath() {
        assertInstanceOf(LoginController.class, mapper.find(POST, "/user/login"));
        assertNull(mapper.find(GET, "/user/login"));
    }

    @Test
    void servesUnmappedStaticFile() throws IOException {
        String response = get("/css/styles.css");

        assertTrue(response.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(response.contains("Content-Type: text/css"));
    }

    @Test
    void returns404WhenNoRouteOrFileExists() throws IOException {
        assertTrue(get("/missing.html").startsWith("HTTP/1.1 404 Not Found\r\n"));
    }

    @Test
    void redirectsUnauthenticatedListRequest() throws IOException {
        String response = get("/user/list.html");

        assertTrue(response.startsWith("HTTP/1.1 302 Found\r\n"));
        assertTrue(response.contains("Location: /user/login.html\r\n"));
    }

    private String get(String path) throws IOException {
        String rawRequest = "GET " + path + " HTTP/1.1\r\nHost: localhost\r\n\r\n";
        HttpRequest request = HttpRequest.from(new BufferedReader(new StringReader(rawRequest)));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        frontController.dispatch(request, new HttpResponse(output));
        return output.toString(StandardCharsets.UTF_8);
    }
}
