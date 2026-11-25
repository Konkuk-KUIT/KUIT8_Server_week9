package http;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    @Test
    void parsesSignupRequest() throws IOException {
        String body = "userId=student&password=pass"
                + "&name=%ED%99%8D+%EA%B8%B8%EB%8F%99"
                + "&email=student%2Btest%40example.com";
        int byteLength = body.getBytes(StandardCharsets.UTF_8).length;

        String rawRequest = "POST /user/signup HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "Content-Type: application/x-www-form-urlencoded\r\n"
                + "Content-Length: " + byteLength + "\r\n"
                + "\r\n"
                + body;

        HttpRequest request = parse(rawRequest);

        assertNotNull(request);
        assertEquals(HttpMethod.POST, request.startLine().method());
        assertEquals("/user/signup", request.startLine().path());
        assertEquals("HTTP/1.1", request.startLine().version());
        assertEquals("localhost:8080", request.headers().get("host"));
        assertEquals("application/x-www-form-urlencoded",
                request.headers().get("Content-Type"));
        assertEquals(byteLength, request.headers().contentLength());

        assertEquals(body, request.body());
        assertEquals(Map.of(
                "userId", "student",
                "password", "pass",
                "name", "홍 길동",
                "email", "student+test@example.com"
        ), request.formParams());
    }

    @Test
    void parsesGetRequestWithoutBody() throws IOException {
        HttpRequest request = parse("GET /index.html?lang=ko HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "\r\n");

        assertNotNull(request);
        assertEquals(HttpMethod.GET, request.startLine().method());
        assertEquals("/index.html", request.startLine().path());
        assertEquals("lang=ko", request.startLine().query());
        assertEquals("", request.body());
        assertTrue(request.formParams().isEmpty());
    }

    @Test
    void rejectsIncompleteBody() {
        String rawRequest = "POST /user/signup HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "Content-Length: 20\r\n"
                + "\r\n"
                + "userId=lee";

        assertThrows(EOFException.class, () -> parse(rawRequest));
    }

    private HttpRequest parse(String rawRequest) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(rawRequest))) {
            return HttpRequest.from(reader);
        }
    }
}
