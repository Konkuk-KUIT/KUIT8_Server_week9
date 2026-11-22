package http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {
    @Test
    void redirectWritesLocationCookieAndEmptyBody() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new HttpResponse(out).redirect("/index.html", "logined=true; Path=/");
        String raw = out.toString(StandardCharsets.UTF_8);

        assertTrue(raw.startsWith("HTTP/1.1 302 Found\r\n"));
        assertTrue(raw.contains("\r\nLocation: /index.html\r\n"));
        assertTrue(raw.contains("\r\nSet-Cookie: logined=true; Path=/\r\n"));
        assertTrue(raw.contains("\r\nContent-Length: 0\r\n"));
        assertTrue(raw.endsWith("\r\n\r\n"));
    }

    @Test
    void redirectWithoutCookieOmitsSetCookie() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new HttpResponse(out).redirect("/user/login_failed.html");
        String raw = out.toString(StandardCharsets.UTF_8);

        assertTrue(raw.contains("\r\nLocation: /user/login_failed.html\r\n"));
        assertFalse(raw.contains("Set-Cookie"));
    }

    @Test
    void forwardWritesHtmlWithByteLengthContentLength() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new HttpResponse(out).forward("/index.html");
        String raw = out.toString(StandardCharsets.ISO_8859_1);

        int headerEnd = raw.indexOf("\r\n\r\n");
        String header = raw.substring(0, headerEnd);
        int bodyLength = raw.length() - (headerEnd + 4);

        assertTrue(header.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(header.contains("\r\nContent-Type: text/html;charset=utf-8\r\n"));
        assertTrue(header.contains("\r\nContent-Length: " + bodyLength + "\r\n"));
        assertEquals(Files.size(Path.of("webapp/index.html")), bodyLength);
    }

    @Test
    void forwardUsesTextCssForStylesheet() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new HttpResponse(out).forward("/css/styles.css");
        String raw = out.toString(StandardCharsets.ISO_8859_1);

        assertTrue(raw.contains("\r\nContent-Type: text/css\r\n"));
    }
}