package http;

import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.StringReader;
import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {
    private HttpRequest read(String request) throws Exception {
        return HttpRequest.from(new BufferedReader(new StringReader(request)));
    }

    @Test
    void readsGetPathQueryAndHeaders() throws Exception {
        HttpRequest request = read("GET /user/form.html?name=Kim HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/user/form.html", request.getPath());
        assertEquals("Kim", request.getParameter("name"));
        assertEquals("localhost", request.getHeader("HOST"));
    }

    @Test
    void readsPostBodyAndDecodesFields() throws Exception {
        String body = "userId=kim&name=%ED%99%8D%EA%B8%B8%EB%8F%99";
        HttpRequest request = read("POST /user/signup HTTP/1.1\r\nContent-Length: "
                + body.length() + "\r\n\r\n" + body);
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/signup", request.getPath());
        assertEquals("kim", request.getParameter("userId"));
        assertEquals("홍길동", request.getParameter("name"));
    }

    @Test
    void checksExactLoginCookieAmongOtherCookies() throws Exception {
        assertTrue(read("GET / HTTP/1.1\r\nCookie: other=1; logined=true\r\n\r\n").isLoggedIn());
        assertFalse(read("GET / HTTP/1.1\r\nCookie: logined=trueish\r\n\r\n").isLoggedIn());
        assertFalse(read("GET / HTTP/1.1\r\n\r\n").isLoggedIn());
    }
}
