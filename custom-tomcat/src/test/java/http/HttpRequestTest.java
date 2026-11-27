
package http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

public class HttpRequestTest {

    private HttpRequest createRequest(String rawRequest) throws IOException {
        BufferedReader reader = new BufferedReader(
                new StringReader(rawRequest)
        );

        return HttpRequest.from(reader);
    }

    @Test
    void getRequestParsesMethodAndPath() throws IOException {

        String rawRequest =
                "GET /index.html HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "\r\n";

        HttpRequest request = createRequest(rawRequest);

        assertNotNull(request);
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath());
        assertEquals("HTTP/1.1", request.getVersion());
    }

    @Test
    void getRequestParsesQueryParameters() throws IOException {

        String rawRequest =
                "GET /user/signup?userId=test123&name=Sieun HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "\r\n";

        HttpRequest request = createRequest(rawRequest);

        assertEquals("/user/signup", request.getPath());
        assertEquals("test123", request.getParameters().get("userId"));
        assertEquals("Sieun", request.getParameters().get("name"));
    }

    @Test
    void postRequestParsesBodyParameters() throws IOException {

        String body = "userId=test123&password=1234";

        String rawRequest =
                "POST /user/login HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "\r\n"
                + body;

        HttpRequest request = createRequest(rawRequest);

        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/login", request.getPath());
        assertEquals(body, request.getBody());
        assertEquals("test123", request.getParameters().get("userId"));
        assertEquals("1234", request.getParameters().get("password"));
    }

    @Test
    void headerNamesAreCaseInsensitive() throws IOException {

        String rawRequest =
                "GET /user/userList HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "cookie: theme=dark; logined=true\r\n"
                + "\r\n";

        HttpRequest request = createRequest(rawRequest);

        assertEquals(
                "theme=dark; logined=true",
                request.getHeader("Cookie")
        );
        assertEquals(
                "theme=dark; logined=true",
                request.getHeader("cookie")
        );
    }

    @Test
    void invalidContentLengthThrowsException() {

        String rawRequest =
                "POST /user/login HTTP/1.1\r\n"
                + "Content-Length: abc\r\n"
                + "\r\n";

        assertThrows(
                IllegalArgumentException.class,
                () -> createRequest(rawRequest)
        );
    }

    @Test
    void unsupportedMethodThrowsException() {

        String rawRequest =
                "UNKNOWN /index.html HTTP/1.1\r\n"
                + "\r\n";

        assertThrows(
                UnsupportedOperationException.class,
                () -> createRequest(rawRequest)
        );
    }
}
