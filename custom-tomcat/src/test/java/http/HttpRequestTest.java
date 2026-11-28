package http;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    @Test
    void parsesGetQuery() throws Exception {
        HttpRequest request = parse(
                "GET /user/signup?userId=minseo&name=Kim+Minseo HTTP/1.1\r\n"
                        + "Host: localhost:8080\r\n"
                        + "\r\n"
        );

        assertNotNull(request);
        assertEquals(HttpMethod.GET, request.method());
        assertEquals("/user/signup", request.path());
        assertEquals("HTTP/1.1", request.version());
        assertEquals("minseo", request.query().get("userId"));
        assertEquals("Kim Minseo", request.query().get("name"));
        assertEquals("localhost:8080", request.header("host"));
        assertTrue(request.form().isEmpty());
    }

    @Test
    void parsesPostForm() throws Exception {
        String body =
                "userId=minseo&password=pass"
                        + "&name=%EA%B9%80%EB%AF%BC%EC%84%9C"
                        + "&email=minseo%40example.com";

        HttpRequest request = parse(
                "POST /user/signup HTTP/1.1\r\n"
                        + "content-length: "
                        + body.getBytes(StandardCharsets.UTF_8).length
                        + "\r\n"
                        + "Content-Type: application/x-www-form-urlencoded\r\n"
                        + "\r\n"
                        + body
        );

        assertNotNull(request);
        assertEquals(HttpMethod.POST, request.method());
        assertEquals("/user/signup", request.path());
        assertEquals("minseo", request.form().get("userId"));
        assertEquals("pass", request.form().get("password"));
        assertEquals("김민서", request.form().get("name"));
        assertEquals(
                "minseo@example.com",
                request.form().get("email")
        );
    }

    @Test
    void recognizesExactLoginCookieAmongMultipleCookies()
            throws Exception {

        HttpRequest request = parse(
                "GET /user/userList HTTP/1.1\r\n"
                        + "Cookie: theme=dark; logined=true; language=ko\r\n"
                        + "\r\n"
        );

        assertTrue(request.isLoggedIn());
    }

    @Test
    void rejectsMissingOrIncorrectLoginCookie() throws Exception {
        HttpRequest missing = parse(
                "GET /user/userList HTTP/1.1\r\n\r\n"
        );

        HttpRequest incorrect = parse(
                "GET /user/userList HTTP/1.1\r\n"
                        + "Cookie: otherlogined=true; logined=true123\r\n"
                        + "\r\n"
        );

        assertFalse(missing.isLoggedIn());
        assertFalse(incorrect.isLoggedIn());
    }

    @Test
    void keepsRequestsSeparate() throws Exception {
        HttpRequest first = parse(
                "GET /?name=minseo HTTP/1.1\r\n"
                        + "Cookie: logined=true\r\n\r\n"
        );

        HttpRequest second = parse(
                "GET /index.html HTTP/1.1\r\n\r\n"
        );

        assertTrue(first.isLoggedIn());
        assertFalse(second.isLoggedIn());
        assertTrue(second.query().isEmpty());
    }

    @Test
    void rejectsIncompletePostBody() {
        assertThrows(
                java.io.EOFException.class,
                () -> parse(
                        "POST /user/signup HTTP/1.1\r\n"
                                + "Content-Length: 100\r\n\r\n"
                                + "userId=minseo"
                )
        );
    }

    private HttpRequest parse(String rawRequest) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new StringReader(rawRequest)
        )) {
            return HttpRequest.from(reader);
        }
    }
}