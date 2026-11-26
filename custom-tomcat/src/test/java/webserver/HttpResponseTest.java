package webserver;

import http.HttpHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpResponseTest {
    private ByteArrayOutputStream out;
    private HttpResponse response;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        response = new HttpResponse(new DataOutputStream(out));
    }

    @Test
    void CSS_파일을_200과_text_css로_응답한다() throws IOException {
        response.forward("/css/styles.css");
        String result = out.toString();

        assertTrue(result.startsWith("HTTP/1.1 200 OK"));
        assertTrue(result.contains("Content-Type: text/css"));
    }

    @Test
    void 리다이렉트는_302와_Location을_보낸다() throws IOException {
        response.redirect("/index.html");
        String result = out.toString();

        assertTrue(result.startsWith("HTTP/1.1 302 Found"));
        assertTrue(result.contains("Location: /index.html"));
        assertTrue(result.contains("Content-Length: 0"));
    }

    @Test
    void 로그인_성공시_Set_Cookie를_보낸다() throws IOException {
        response.addHeader(HttpHeader.SET_COOKIE, "logined=true; Path=/");
        response.redirect("/index.html");

        assertTrue(out.toString().contains("Set-Cookie: logined=true; Path=/"));
    }
}