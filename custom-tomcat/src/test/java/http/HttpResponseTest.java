package http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {

    @TempDir
    Path tempDir;

    @Test
    void forwardsHtmlWithByteContentLength() throws IOException {
        assertForward("index.html", "<p>안녕하세요</p>", "text/html");
    }

    @Test
    void forwardsCssWithCssContentType() throws IOException {
        assertForward("styles.css", "body { color: red; }", "text/css");
    }

    @Test
    void redirectsWithoutBody() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new HttpResponse(out).redirect("/user/login.html");

        ResponseParts response = splitResponse(out);

        assertTrue(response.head().startsWith("HTTP/1.1 302 Found\r\n"));
        assertTrue(response.head().contains("\r\nLocation: /user/login.html\r\n"));
        assertTrue(response.head().contains("\r\nContent-Length: 0\r\n"));
        assertFalse(response.head().contains("\r\nSet-Cookie:"));
        assertEquals(0, response.body().length);
    }

    @Test
    void redirectsWithLoginCookie() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new HttpResponse(out).redirect("/index.html", "logined=true; Path=/");

        ResponseParts response = splitResponse(out);

        assertTrue(response.head().startsWith("HTTP/1.1 302 Found\r\n"));
        assertTrue(response.head().contains("\r\nLocation: /index.html\r\n"));
        assertTrue(response.head().contains("\r\nSet-Cookie: logined=true; Path=/\r\n"));
        assertTrue(response.head().contains("\r\nContent-Length: 0\r\n"));
        assertEquals(0, response.body().length);
    }

    private void assertForward(String filename, String content, String contentType)
            throws IOException {
        byte[] expectedBody = content.getBytes(StandardCharsets.UTF_8);
        Path file = tempDir.resolve(filename);
        Files.write(file, expectedBody);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new HttpResponse(out).forward(file);

        ResponseParts response = splitResponse(out);

        assertTrue(response.head().startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(response.head().contains(
                "\r\nContent-Type: " + contentType + ";charset=utf-8\r\n"));
        assertTrue(response.head().contains(
                "\r\nContent-Length: " + expectedBody.length + "\r\n"));
        assertArrayEquals(expectedBody, response.body());
    }

    private ResponseParts splitResponse(ByteArrayOutputStream out) {
        byte[] bytes = out.toByteArray();
        String raw = new String(bytes, StandardCharsets.ISO_8859_1);
        int boundary = raw.indexOf("\r\n\r\n");
        assertTrue(boundary >= 0, "응답 헤더와 본문 사이에 빈 줄이 있어야 한다");

        return new ResponseParts(
                raw.substring(0, boundary + 2),
                Arrays.copyOfRange(bytes, boundary + 4, bytes.length)
        );
    }

    private record ResponseParts(String head, byte[] body) {}
}
