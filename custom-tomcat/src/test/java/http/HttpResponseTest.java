package http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {

    @TempDir
    Path tempDir;

    @Test
    void HTML_파일을_정상적으로_반환한다() throws Exception {

        String html = "<h1>안녕하세요</h1>";

        Files.writeString(
                tempDir.resolve("hello.html"),
                html,
                StandardCharsets.UTF_8
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output, tempDir);

        response.forward("/hello.html");

        String result = output.toString(StandardCharsets.UTF_8);
        byte[] expected = html.getBytes(StandardCharsets.UTF_8);

        assertTrue(result.startsWith("HTTP/1.1 200 OK"));
        assertTrue(result.contains("Content-Type: text/html"));
        assertTrue(result.contains("Content-Length: " + expected.length));

        assertArrayEquals(expected, extractBody(output.toByteArray()));
    }

    @Test
    void CSS_파일은_textCss로_반환한다() throws Exception {

        Path cssDir = Files.createDirectory(tempDir.resolve("css"));

        String css = "body { color: red; }";

        Files.writeString(
                cssDir.resolve("styles.css"),
                css,
                StandardCharsets.UTF_8
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output, tempDir);

        response.forward("/css/styles.css");

        String result = output.toString(StandardCharsets.UTF_8);
        byte[] expected = css.getBytes(StandardCharsets.UTF_8);

        assertTrue(result.startsWith("HTTP/1.1 200 OK"));
        assertTrue(result.contains("Content-Type: text/css"));
        assertTrue(result.contains("Content-Length: " + expected.length));

        assertArrayEquals(expected, extractBody(output.toByteArray()));
    }

    @Test
    void 리다이렉트와_쿠키_헤더를_확인한다() throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output, tempDir);

        // 일반 리다이렉트
        response.redirect("/index.html");

        String result = output.toString(StandardCharsets.UTF_8);

        assertTrue(result.startsWith("HTTP/1.1 302 Found"));
        assertTrue(result.contains("Location: /index.html"));
        assertTrue(result.contains("Content-Length: 0"));
        assertFalse(result.contains("Set-Cookie:"));

        // 쿠키를 포함한 리다이렉트
        output.reset();

        response.redirect(
                "/index.html",
                Map.of("Set-Cookie", "logined=true; Path=/")
        );

        String loginResult = output.toString(StandardCharsets.UTF_8);

        assertTrue(loginResult.contains("Location: /index.html"));
        assertTrue(loginResult.contains(
                "Set-Cookie: logined=true; Path=/"));

        assertEquals(0, extractBody(output.toByteArray()).length);
    }

    // HTTP 헤더와 본문 사이의 CRLF CRLF를 찾아 본문 추출
    private byte[] extractBody(byte[] response) {

        String text = new String(response, StandardCharsets.UTF_8);
        int boundary = text.indexOf("\r\n\r\n");

        assertTrue(boundary >= 0, "HTTP 헤더 구분자가 있어야 함");

        // 이번 테스트의 응답 헤더는 모두 ASCII이므로
        // 문자열 위치와 바이트 위치가 동일함
        return Arrays.copyOfRange(
                response, boundary + 4, response.length);
    }
}
