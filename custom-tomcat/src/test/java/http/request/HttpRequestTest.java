package http.request;

import http.constant.HttpMethod;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    private BufferedReader readerOf(String request) {
        return new BufferedReader(new StringReader(request));
    }

    @Test
    void POST_회원가입_요청을_분석한다() throws IOException {
        // "김 철수"를 폼 인코딩하면 %EA%B9%80+%EC%B2%A0%EC%88%98
        String body = "userId=student&password=pass&name=%EA%B9%80+%EC%B2%A0%EC%88%98&email=student%40example.com";
        String request = "POST /user/signup HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "Content-Type: application/x-www-form-urlencoded\r\n"
                + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n"
                + body;

        HttpRequest httpRequest = HttpRequest.from(readerOf(request));

        assertEquals(HttpMethod.POST, httpRequest.getMethod());
        assertEquals("/user/signup", httpRequest.getPath());
        assertEquals("HTTP/1.1", httpRequest.getVersion());
        assertEquals("localhost:8080", httpRequest.getHeader("Host"));
        assertEquals("student", httpRequest.getParameter("userId"));
        assertEquals("pass", httpRequest.getParameter("password"));
        assertEquals("김 철수", httpRequest.getParameter("name"));
        assertEquals("student@example.com", httpRequest.getParameter("email"));
    }

    @Test
    void GET_요청은_경로와_쿼리스트링을_나눈다() throws IOException {
        String request = "GET /user/signup?userId=kim&password=1234 HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "\r\n";

        HttpRequest httpRequest = HttpRequest.from(readerOf(request));

        assertEquals(HttpMethod.GET, httpRequest.getMethod());
        assertEquals("/user/signup", httpRequest.getPath());
        assertEquals("kim", httpRequest.getParameter("userId"));
        assertEquals("1234", httpRequest.getParameter("password"));
    }

    @Test
    void 헤더_이름은_대소문자를_구분하지_않는다() throws IOException {
        String request = "GET /index.html HTTP/1.1\r\n"
                + "content-type: text/html\r\n"
                + "\r\n";

        HttpRequest httpRequest = HttpRequest.from(readerOf(request));

        assertEquals("text/html", httpRequest.getHeader("Content-Type"));
    }

    @Test
    void 여러_쿠키_중_logined_값을_정확히_찾는다() throws IOException {
        String request = "GET /user/userList HTTP/1.1\r\n"
                + "Cookie: notlogined=false; logined=true; theme=dark\r\n"
                + "\r\n";

        HttpRequest httpRequest = HttpRequest.from(readerOf(request));

        assertEquals("true", httpRequest.getCookie("logined"));
        assertNull(httpRequest.getCookie("없는쿠키"));
    }

    @Test
    void 쿠키가_없으면_null이다() throws IOException {
        String request = "GET /user/userList HTTP/1.1\r\n"
                + "\r\n";

        assertNull(HttpRequest.from(readerOf(request)).getCookie("logined"));
    }

    @Test
    void 본문이_Content_Length보다_짧으면_예외가_난다() {
        String request = "POST /user/signup HTTP/1.1\r\n"
                + "Content-Length: 50\r\n"
                + "\r\n"
                + "userId=short";

        assertThrows(EOFException.class, () -> HttpRequest.from(readerOf(request)));
    }
}
