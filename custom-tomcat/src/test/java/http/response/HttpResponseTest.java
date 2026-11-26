package http.response;

import http.constant.HttpHeader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {

    // 응답 바이트에서 "\r\n\r\n" 앞부분(상태 줄 + 헤더)만 문자열로 꺼낸다
    private String headPart(byte[] response) {
        String all = new String(response, StandardCharsets.ISO_8859_1);
        return all.substring(0, all.indexOf("\r\n\r\n"));
    }

    // 응답 바이트에서 "\r\n\r\n" 뒷부분(본문)만 바이트로 꺼낸다
    private byte[] bodyPart(byte[] response) {
        String all = new String(response, StandardCharsets.ISO_8859_1);
        int bodyStart = all.indexOf("\r\n\r\n") + 4;
        return Arrays.copyOfRange(response, bodyStart, response.length);
    }

    @Test
    void forward는_HTML_파일을_200으로_응답한다() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.forward("/index.html");

        byte[] expectedBody = Files.readAllBytes(Paths.get("webapp/index.html"));
        String head = headPart(out.toByteArray());
        assertTrue(head.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(head.contains("Content-Type: text/html;charset=utf-8"));
        assertTrue(head.contains("Content-Length: " + expectedBody.length));
        assertArrayEquals(expectedBody, bodyPart(out.toByteArray()));
    }

    @Test
    void forward는_CSS_파일을_text_css로_응답한다() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.forward("/css/styles.css");

        byte[] expectedBody = Files.readAllBytes(Paths.get("webapp/css/styles.css"));
        String head = headPart(out.toByteArray());
        assertTrue(head.contains("Content-Type: text/css"));
        assertTrue(head.contains("Content-Length: " + expectedBody.length));
        assertArrayEquals(expectedBody, bodyPart(out.toByteArray()));
    }

    @Test
    void redirect는_302와_Location을_응답한다() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.redirect("/index.html");

        String head = headPart(out.toByteArray());
        assertTrue(head.startsWith("HTTP/1.1 302 Found\r\n"));
        assertTrue(head.contains("Location: /index.html"));
        assertTrue(head.contains("Content-Length: 0"));
        assertEquals(0, bodyPart(out.toByteArray()).length);
    }

    @Test
    void 로그인_성공_리다이렉트에는_Set_Cookie가_함께_간다() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.addHeader(HttpHeader.SET_COOKIE, "logined=true; Path=/");
        response.redirect("/index.html");

        String head = headPart(out.toByteArray());
        assertTrue(head.contains("Set-Cookie: logined=true; Path=/"));
        assertTrue(head.contains("Location: /index.html"));
    }

    @Test
    void 없는_파일은_404로_응답한다() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        response.forward("/nothing.html");

        assertTrue(headPart(out.toByteArray()).startsWith("HTTP/1.1 404 Not Found\r\n"));
    }
}
