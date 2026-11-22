package http;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {
    private static HttpRequest parse(String raw) throws IOException {
        return HttpRequest.from(new BufferedReader(new StringReader(raw)));
    }

    @Test
    void parsesGetMethodPathQueryAndHeaders() throws IOException {
        HttpRequest request = parse("GET /user/signup?userId=student&name=kimsubin HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Cookie: logined=true\r\n" +
                "\r\n");

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/user/signup", request.getPath());
        assertEquals("student", request.getParameter("userId"));
        assertEquals("kimsubin", request.getParameter("name"));
        assertEquals("localhost:8080", request.getHeader("HOST"));
        assertEquals("logined=true", request.getHeader("cookie"));
        assertEquals("", request.getHeader("missing"));
    }

    @Test
    void readsPostFormBodyUsingContentLength() throws IOException {
        String body = "userId=student&name=kimsubin&email=subin%40abc.com";
        HttpRequest request = parse("POST /user/signup HTTP/1.1\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" + body);

        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/signup", request.getPath());
        assertEquals("student", request.getParameter("userId"));
        assertEquals("kimsubin", request.getParameter("name"));
        assertEquals("subin@abc.com", request.getParameter("email"));
    }

    @Test
    void returnsNullWhenInputIsEmpty() throws IOException {
        assertNull(parse(""));
    }
}