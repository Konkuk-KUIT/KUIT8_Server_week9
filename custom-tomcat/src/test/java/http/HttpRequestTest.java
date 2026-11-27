package http;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    private HttpRequest parse(String raw) throws Exception {
        return HttpRequest.from(new BufferedReader(new StringReader(raw)));
    }

    @Test
    void getRequest() throws Exception {
        HttpRequest request = parse(
                "GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n"
        );

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath());
        assertEquals("localhost", request.getHeader("Host"));
    }

    @Test
    void getParameters() throws Exception {
        HttpRequest request = parse(
                "GET /user/signup?userId=test&name=kim HTTP/1.1\r\n\r\n"
        );

        assertEquals("/user/signup", request.getPath());
        assertEquals("test", request.getParameter("userId"));
        assertEquals("kim", request.getParameter("name"));
    }

    @Test
    void postParameters() throws Exception {
        String body = "userId=test&password=1234";

        HttpRequest request = parse(
                "POST /user/login HTTP/1.1\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "\r\n"
                + body
        );

        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("test", request.getParameter("userId"));
        assertEquals("1234", request.getParameter("password"));
    }

    @Test
    void cookie() throws Exception {
        HttpRequest request = parse(
                "GET /user/userList HTTP/1.1\r\n"
                + "Cookie: theme=dark; logined=true\r\n\r\n"
        );

        assertTrue(request.isLogined());
    }

    @Test
    void noLoginCookie() throws Exception {
        HttpRequest request = parse(
                "GET /user/userList HTTP/1.1\r\n\r\n"
        );

        assertFalse(request.isLogined());
    }

    @Test
    void falseLoginCookie() throws Exception {
        HttpRequest request = parse(
                "GET /user/userList HTTP/1.1\r\n"
                + "Cookie: logined=false\r\n\r\n"
        );

        assertFalse(request.isLogined());
    }
}