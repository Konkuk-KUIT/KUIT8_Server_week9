package webserver;

import controller.ListController;
import controller.LoginController;
import controller.StaticFileController;
import db.MemoryUserRepository;
import http.HttpMethod;
import http.HttpStartLine;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestMapperTest {
    private final RequestMapper mapper = new RequestMapper();

    @Test
    void selectsLoginControllerForPost() {
        LoginController login = new LoginController(MemoryUserRepository.getInstance());
        mapper.register(HttpMethod.POST, "/user/login", login);

        HttpStartLine startLine = HttpStartLine.parse("POST /user/login HTTP/1.1");

        assertSame(login, mapper.find(startLine));
    }

    @Test
    void servesUnmappedCssFile() {
        String response = sendGet("/css/styles.css");
        String[] parts = response.split("\r\n\r\n", 2);

        assertTrue(parts[0].startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(parts[0].contains("\r\nContent-Type: text/css"));
        assertTrue(parts.length == 2 && !parts[1].isEmpty());
    }

    @Test
    void returns404ForMissingFile() {
        String response = sendGet("/missing-page.html");

        assertTrue(response.startsWith("HTTP/1.1 404 Not Found\r\n"));
    }

    @Test
    void redirectsListRequestWithoutCookie() {
        mapper.register(HttpMethod.GET, "/user/list.html", new ListController());

        String response = sendGet("/user/list.html");

        assertTrue(response.startsWith("HTTP/1.1 302 Found\r\n"));
        assertTrue(response.contains("\r\nLocation: /user/login.html\r\n"));
    }

    private String sendGet(String path) {
        String request = "GET " + path + " HTTP/1.1\r\n\r\n";
        ByteArrayInputStream in =
                new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Socket socket = new Socket() {
            @Override
            public InputStream getInputStream() {
                return in;
            }

            @Override
            public OutputStream getOutputStream() {
                return out;
            }
        };

        new RequestHandler(socket, mapper, new StaticFileController()).run();
        return out.toString(StandardCharsets.UTF_8);
    }
}