package webserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpResponseTest {
    @TempDir
    Path directory;

    @Test
    void forwardsHtml() throws IOException {
        assertForward("index.html", "text/html", "<p>안녕하세요</p>");
    }

    @Test
    void forwardsCss() throws IOException {
        assertForward("styles.css", "text/css", "p::after { content: '안녕'; }");
    }

    @Test
    void redirectsWithLocationAndEmptyBody() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new HttpResponse(output).redirect("/user/login.html");

        assertResponse(output, "HTTP/1.1 302 Found", new byte[0], "Location: /user/login.html");
    }

    @Test
    void redirectsAfterLoginWithCookie() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new HttpResponse(output).redirect("/index.html", "logined=true; Path=/");

        assertResponse(output, "HTTP/1.1 302 Found", new byte[0],
                "Location: /index.html", "Set-Cookie: logined=true; Path=/");
    }

    private void assertForward(String name, String type, String content) throws IOException {
        byte[] expected = content.getBytes(StandardCharsets.UTF_8);
        Path file = Files.write(directory.resolve(name), expected);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new HttpResponse(output).forward(file);

        assertResponse(output, "HTTP/1.1 200 OK", expected, "Content-Type: " + type + "; charset=UTF-8");
    }

    private void assertResponse(ByteArrayOutputStream output, String status, byte[] body, String... headers) {
        byte[] bytes = output.toByteArray();
        String message = output.toString(StandardCharsets.ISO_8859_1);
        int boundary = message.indexOf("\r\n\r\n");
        assertTrue(boundary >= 0, "Missing header terminator");
        var lines = Arrays.asList(message.substring(0, boundary).split("\r\n"));
        org.junit.jupiter.api.Assertions.assertEquals(status, lines.get(0));
        assertTrue(lines.contains("Content-Length: " + body.length));
        assertTrue(lines.contains("Connection: close"));
        for (String header : headers) {
            assertTrue(lines.contains(header), header);
        }
        assertArrayEquals(body, Arrays.copyOfRange(bytes, boundary + 4, bytes.length));
    }
}
