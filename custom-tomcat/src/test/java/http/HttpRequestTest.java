package http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {
    @Test
    void readsPostHeadersAndDecodesAllFourFormFields() throws Exception {
        String body = "userId=student&password=p%2B123%3Dok"
                + "&name=%ED%99%8D+%EA%B8%B8%EB%8F%99&email=student%2Btest%40example.com";
        HttpRequest request = parse(post("/user/signup?userId=ignored", body));

        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/signup", request.getPath());
        assertEquals("HTTP/1.1", request.getVersion());
        assertEquals("localhost:8080", request.getHeader("HOST"));
        assertEquals("application/x-www-form-urlencoded", request.getHeader("Content-Type"));
        assertEquals(Integer.toString(body.getBytes(StandardCharsets.UTF_8).length), request.getHeader("content-length"));
        assertEquals(body, request.getBody());
        assertEquals(Map.of("userId", "student", "password", "p+123=ok",
                "name", "홍 길동", "email", "student+test@example.com"), request.getParameters());
    }

    @Test
    void readsGetParametersFromTheQuery() throws Exception {
        HttpRequest request = parse("GET /user/signup?userId=student&name=Sample+User HTTP/1.1\r\n\r\n");

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/user/signup", request.getPath());
        assertEquals(Map.of("userId", "student", "name", "Sample User"), request.getParameters());
        assertEquals("", request.getBody());
    }

    @Test
    void readsCookiesAcrossMultipleHeadersWithoutConfusingNames() throws Exception {
        HttpRequest request = parse("GET / HTTP/1.1\r\n"
                + "Cookie: notlogined=true; logined=false\r\n"
                + "cookie: theme=dark; token=a=b\r\n\r\n");

        assertEquals("false", request.getCookie("logined"));
        assertEquals("dark", request.getCookie("theme"));
        assertEquals("a=b", request.getCookie("token"));
        assertNull(request.getCookie("missing"));
    }

    @Test
    void consumesOnlyTheDeclaredBodyAndKeepsRequestsIndependent() throws Exception {
        String body = "userId=first";
        String firstRaw = "POST /user/signup HTTP/1.1\r\nContent-Length: " + body.length()
                + "\r\nCookie: logined=true\r\n\r\n" + body;
        BufferedReader reader = new BufferedReader(new StringReader(firstRaw + "GET / HTTP/1.1\r\n\r\n"));

        HttpRequest first = HttpRequest.from(reader);
        HttpRequest second = HttpRequest.from(reader);

        assertEquals(Map.of("userId", "first"), first.getParameters());
        assertEquals("true", first.getCookie("logined"));
        assertEquals(HttpMethod.GET, second.getMethod());
        assertEquals("/", second.getPath());
        assertNull(second.getHeader("Content-Length"));
        assertNull(second.getCookie("logined"));
        assertEquals("", second.getBody());
        assertTrue(second.getParameters().isEmpty());
    }

    @Test
    void rejectsHeadersThatEndBeforeTheBlankLine() {
        assertThrows(EOFException.class, () -> parse("GET / HTTP/1.1\r\nHost: localhost\r\n"));
    }

    @Test
    void rejectsABodyShorterThanContentLength() {
        assertThrows(EOFException.class, () -> parse("POST /user/signup HTTP/1.1\r\nContent-Length: 5\r\n\r\nab"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "invalid"})
    void rejectsInvalidContentLengths(String length) {
        assertThrows(IllegalArgumentException.class,
                () -> parse("POST /user/signup HTTP/1.1\r\nContent-Length: " + length + "\r\n\r\n"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"name=%ZZ", "userId=first&userId=second"})
    void rejectsMalformedOrDuplicateFormParameters(String body) throws Exception {
        HttpRequest request = parse(post("/user/signup", body));
        assertThrows(IllegalArgumentException.class, request::getParameters);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\r\n"})
    void returnsNoRequestForEmptyInput(String input) throws Exception {
        assertNull(parse(input));
    }

    private HttpRequest parse(String raw) throws Exception {
        return HttpRequest.from(new BufferedReader(new StringReader(raw)));
    }

    private String post(String path, String body) {
        return "POST " + path + " HTTP/1.1\r\nHost: localhost:8080\r\n"
                + "Content-Type: application/x-www-form-urlencoded\r\n"
                + "cOnTeNt-LeNgTh:  " + body.getBytes(StandardCharsets.UTF_8).length + "  \r\n\r\n" + body;
    }
}
