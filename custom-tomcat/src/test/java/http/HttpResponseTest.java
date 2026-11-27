
package http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class HttpResponseTest {

    @Test
    void redirectReturns302AndLocation() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.redirect("/index.html");

        String result = out.toString(StandardCharsets.UTF_8);

        assertTrue(result.startsWith("HTTP/1.1 302 Found\r\n"));
        assertTrue(result.contains("Location: /index.html\r\n"));
        assertTrue(result.contains("Content-Length: 0\r\n"));
        assertTrue(result.contains("Connection: close\r\n"));
    }

    @Test
    void loginRedirectIncludesCookie() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.redirect("/index.html", "logined=true; Path=/");

        String result = out.toString(StandardCharsets.UTF_8);

        assertTrue(result.startsWith("HTTP/1.1 302 Found\r\n"));
        assertTrue(result.contains("Location: /index.html\r\n"));
        assertTrue(result.contains(
                "Set-Cookie: logined=true; Path=/\r\n"
        ));
    }

    @Test
    void forwardReturnsHtmlFile() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.forward("/index.html");

        String result = out.toString(StandardCharsets.UTF_8);

        assertTrue(result.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(result.contains(
                "Content-Type: text/html; charset=utf-8\r\n"
        ));
    }

    @Test
    void forwardReturnsCssWithCorrectContentLength() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.forward("/css/styles.css");

        byte[] result = out.toByteArray();
        String responseText = new String(
                result, StandardCharsets.ISO_8859_1
        );

        byte[] expectedBody = Files.readAllBytes(
                Paths.get("webapp/css/styles.css")
        );

        int headerEnd = responseText.indexOf("\r\n\r\n");

        assertTrue(headerEnd >= 0);
        assertTrue(responseText.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(responseText.contains(
                "Content-Type: text/css; charset=utf-8\r\n"
        ));
        assertTrue(responseText.contains(
                "Content-Length: " + expectedBody.length + "\r\n"
        ));

        byte[] actualBody = java.util.Arrays.copyOfRange(
                result,
                headerEnd + 4,
                result.length
        );

        assertArrayEquals(expectedBody, actualBody);
    }

    @Test
    void missingFileReturns404() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.forward("/missing-file-test.html");

        String result = out.toString(StandardCharsets.UTF_8);

        assertTrue(result.startsWith("HTTP/1.1 404 Not Found\r\n"));
    }

    @Test
    void errorReturnsCorrectStatus() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.error(HttpStatus.BAD_REQUEST);

        String result = out.toString(StandardCharsets.UTF_8);

        assertTrue(result.startsWith("HTTP/1.1 400 Bad Request\r\n"));
        assertTrue(result.endsWith("400 Bad Request"));
    }
}
