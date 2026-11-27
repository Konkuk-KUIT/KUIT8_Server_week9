package http;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {
    @Test
    void sendsHtmlAndCssWithCorrectHeadersAndBytes() throws Exception {
        checkFile("/", "webapp/index.html", "text/html");
        checkFile("/css/styles.css", "webapp/css/styles.css", "text/css");
    }

    private void checkFile(String path, String file, String contentType) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new HttpResponse(new DataOutputStream(output)).forward(path);
        byte[] response = output.toByteArray();
        String text = new String(response, StandardCharsets.ISO_8859_1);
        int bodyStart = text.indexOf("\r\n\r\n") + 4;
        byte[] body = Files.readAllBytes(Path.of(file));
        assertTrue(text.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(text.contains("Content-Type: " + contentType));
        assertTrue(text.contains("Content-Length: " + body.length + "\r\n"));
        assertArrayEquals(body, Arrays.copyOfRange(response, bodyStart, response.length));
    }

    @Test
    void redirectsWithLocationAndCookie() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(new DataOutputStream(output));
        response.addHeader("Set-Cookie", "logined=true; Path=/");
        response.redirect("/index.html");
        String result = output.toString(StandardCharsets.UTF_8);
        assertTrue(result.startsWith("HTTP/1.1 302 Found\r\n"));
        assertTrue(result.contains("Location: /index.html\r\n"));
        assertTrue(result.contains("Set-Cookie: logined=true; Path=/\r\n"));
        assertTrue(result.contains("Content-Length: 0\r\n"));
        assertTrue(result.endsWith("\r\n\r\n"));
    }
}
