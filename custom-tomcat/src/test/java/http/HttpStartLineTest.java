package http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpStartLineTest {
    @Test
    void readsMethodPathAndVersion() {
        HttpStartLine line = HttpStartLine.from("GET /index.html HTTP/1.1");

        assertEquals(HttpMethod.GET, line.getMethod());
        assertEquals("/index.html", line.getPath());
        assertEquals("HTTP/1.1", line.getVersion());
        assertEquals("", line.getQueryString());
    }

    @Test
    void separatesQueryWithoutDecodingItEarly() {
        HttpStartLine line = HttpStartLine.from("GET /user/signup?name=A%2BB&email=a%40b.com HTTP/1.1");

        assertEquals("/user/signup", line.getPath());
        assertEquals("name=A%2BB&email=a%40b.com", line.getQueryString());
    }

    @Test
    void rejectsAnIncompleteStartLine() {
        assertThrows(IllegalArgumentException.class, () -> HttpStartLine.from("GET /index.html"));
    }
}
