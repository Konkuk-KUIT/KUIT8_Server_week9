package http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {

    @Test
    void forwardsHtml() throws Exception {
        assertFileResponse(
                "/index.html",
                "text/html; charset=utf-8"
        );
    }

    @Test
    void forwardsCss() throws Exception {
        assertFileResponse(
                "/css/styles.css",
                "text/css; charset=utf-8"
        );
    }

    @Test
    void redirectsWithLocation() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output);

        response.redirect("/user/login.html");

        String actual = output.toString(StandardCharsets.UTF_8);

        assertEquals(
                "HTTP/1.1 302 Found\r\n"
                        + "Location: /user/login.html\r\n"
                        + "Content-Length: 0\r\n"
                        + "Connection: close\r\n"
                        + "\r\n",
                actual
        );
    }

    @Test
    void redirectsWithLoginCookie() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output);

        response.redirect("/index.html", "logined=true; Path=/");

        String actual = output.toString(StandardCharsets.UTF_8);

        assertEquals(
                "HTTP/1.1 302 Found\r\n"
                        + "Location: /index.html\r\n"
                        + "Set-Cookie: logined=true; Path=/\r\n"
                        + "Content-Length: 0\r\n"
                        + "Connection: close\r\n"
                        + "\r\n",
                actual
        );
    }

    private void assertFileResponse(
            String url,
            String contentType
    ) throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output);

        response.forward(url);

        byte[] actual = output.toByteArray();
        byte[] expectedBody = Files.readAllBytes(
                Path.of("webapp").resolve(url.substring(1))
        );

        String expectedHeaders =
                "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: " + contentType + "\r\n"
                        + "Content-Length: " + expectedBody.length + "\r\n"
                        + "Connection: close\r\n"
                        + "\r\n";

        byte[] expectedHeaderBytes =
                expectedHeaders.getBytes(StandardCharsets.US_ASCII);

        assertEquals(
                expectedHeaderBytes.length + expectedBody.length,
                actual.length
        );

        assertArrayEquals(
                expectedHeaderBytes,
                Arrays.copyOfRange(
                        actual,
                        0,
                        expectedHeaderBytes.length
                )
        );

        assertArrayEquals(
                expectedBody,
                Arrays.copyOfRange(
                        actual,
                        expectedHeaderBytes.length,
                        actual.length
                )
        );
    }
}