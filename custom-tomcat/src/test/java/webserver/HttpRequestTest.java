package webserver;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpRequestTest {
    @Test
    void parsesSignupRequest() throws IOException {
        String body = "userId=student&password=pass&name=Sample&email=student%40example.com";
        int length = body.getBytes(StandardCharsets.UTF_8).length;
        String message = "POST /user/signup HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "Content-Type: application/x-www-form-urlencoded\r\n"
                + "Content-Length: " + length + "\r\n\r\n"
                + body;

        HttpRequest request = HttpRequest.from(new BufferedReader(new StringReader(message)));

        assertNotNull(request);
        assertEquals(HttpMethod.POST, request.method());
        assertEquals("/user/signup", request.path());
        assertEquals("HTTP/1.1", request.version());
        assertEquals("localhost:8080", request.header("host"));
        assertEquals("application/x-www-form-urlencoded", request.header("Content-Type"));
        assertEquals(Integer.toString(length), request.header("Content-Length"));
        assertEquals(Map.of("userId", "student", "password", "pass",
                "name", "Sample", "email", "student@example.com"), request.form());
    }
}
