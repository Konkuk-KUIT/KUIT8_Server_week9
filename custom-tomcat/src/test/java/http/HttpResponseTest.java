package http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {

    @Test
    void redirect() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(new DataOutputStream(out));

        response.redirect("/index.html");

        String result = out.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("302 Found"));
        assertTrue(result.contains("Location: /index.html"));
        assertTrue(result.contains("Content-Length: 0"));
    }

    @Test
    void redirectWithCookie() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(new DataOutputStream(out));

        response.redirect("/index.html", "logined=true; Path=/");

        String result = out.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("Set-Cookie: logined=true; Path=/"));
    }

    @Test
    void forwardHtml() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(new DataOutputStream(out));

        response.forward("/index.html");

        String result = out.toString(StandardCharsets.UTF_8);
        int length = Files.readAllBytes(Path.of("webapp/index.html")).length;

        assertTrue(result.contains("200 OK"));
        assertTrue(result.contains("Content-Type: text/html"));
        assertTrue(result.contains("Content-Length: " + length));
    }

    @Test
    void forwardCss() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(new DataOutputStream(out));

        response.forward("/css/styles.css");

        String result = out.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("200 OK"));
        assertTrue(result.contains("Content-Type: text/css"));
    }
}