package webserver;

import http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpRequestTest {

    @Test
    private HttpRequest requestOf(String raw) throws IOException {
        return new HttpRequest(new BufferedReader(new StringReader(raw)));
    }

    @Test
    void 요청라인과_헤더를_파싱한다() throws IOException {
        HttpRequest request = requestOf(
                "GET /index.html HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "Cookie: logined=true\r\n" +
                        "\r\n");

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getUrl());
        assertEquals("localhost:8080", request.getHeader().get("Host"));
        assertEquals("true", request.getCookie("logined"));
    }

    @Test
    void POST_본문의_폼_데이터를_파싱한다() throws IOException {
        String body = "userId=minhong&password=1234";
        HttpRequest request = requestOf(
                "POST /user/login HTTP/1.1\r\n" +
                        "Content-Length: " + body.length() + "\r\n" +
                        "\r\n" +
                        body);

        assertEquals("minhong", request.getParameter().get("userId"));
        assertEquals("1234", request.getParameter().get("password"));
    }

    @Test
    void 본문이_Content_Length보다_짧으면_예외() {
        assertThrows(IOException.class, () -> requestOf(
                "POST /user/signup HTTP/1.1\r\n" +
                        "Content-Length: 100\r\n" +
                        "\r\n" +
                        "userId=a"));
    }
}