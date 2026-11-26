package http;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    @Test
    void GET_요청을_분석한다() throws Exception {

        String rawRequest =
                "GET /user/form.html?userId=kim%2Bjun HTTP/1.1\r\n"
                        + "Host: localhost:8080\r\n"
                        + "Cookie: logined=true\r\n"
                        + "\r\n";

        BufferedReader reader = new BufferedReader(new StringReader(rawRequest));

        HttpRequest request = HttpRequest.from(reader);

        assertNotNull(request);
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/user/form.html", request.getPath());
        assertEquals("HTTP/1.1", request.getVersion());

        // 헤더 이름은 대소문자를 구분하지 않음
        assertEquals("localhost:8080", request.getHeader("host"));
        assertEquals("logined=true", request.getHeader("Cookie"));

        // %2B가 +로 디코딩되는지 확인
        assertEquals("kim+jun", request.getParameter("userId"));
    }

    @Test
    void POST_요청의_본문을_분석한다() throws Exception {

        String body =
                "userId=student"
                        + "&password=pass"
                        + "&name=" + URLEncoder.encode(
                        "홍길동", StandardCharsets.UTF_8)
                        + "&email=student%40example.com";

        String rawRequest =
                "POST /user/signup HTTP/1.1\r\n"
                        + "Host: localhost:8080\r\n"
                        + "Content-Type: application/x-www-form-urlencoded\r\n"
                        + "Content-Length: "
                        + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                        + "\r\n"
                        + body;

        BufferedReader reader = new BufferedReader(new StringReader(rawRequest));

        HttpRequest request = HttpRequest.from(reader);

        assertNotNull(request);
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/signup", request.getPath());

        assertEquals("student", request.getParameter("userId"));
        assertEquals("pass", request.getParameter("password"));
        assertEquals("홍길동", request.getParameter("name"));
        assertEquals(
                "student@example.com", request.getParameter("email"));
    }

    @Test
    void POST에서_ContentLength가_없으면_예외가_발생한다() {

        String rawRequest =
                "POST /user/signup HTTP/1.1\r\n"
                        + "Host: localhost:8080\r\n"
                        + "\r\n";

        BufferedReader reader = new BufferedReader(new StringReader(rawRequest));

        assertThrows(
                IllegalArgumentException.class,
                () -> HttpRequest.from(reader)
        );
    }
}

