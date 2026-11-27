package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpResponse {
    // 브라우저로 응답을 보내는 통로
    private final DataOutputStream dos;

    // 응답에 넣을 쿠키. 처음에는 null
    private String cookie;

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }

    // 쿠키를 저장해 둠
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    // HTML이나 CSS 파일을 읽어서 200 응답으로 보냄
    public void forward(String path) throws IOException {
        byte[] body = Files.readAllBytes(Path.of("webapp" + path));

        String contentType = "text/html;charset=utf-8";
        if (path.endsWith(".css")) {
            contentType = "text/css";
        }

        writeStatusLine(HttpStatus.OK);
        dos.writeBytes("Content-Type: " + contentType + "\r\n");

        writeCookie();

        dos.writeBytes("Content-Length: " + body.length + "\r\n");
        dos.writeBytes("\r\n");
        dos.write(body);
        dos.flush();
    }

    // 브라우저에 302와 이동할 주소를 보냄
    public void redirect(String path) throws IOException {
        writeStatusLine(HttpStatus.FOUND);
        dos.writeBytes("Location: " + path + "\r\n");

        writeCookie();

        dos.writeBytes("Content-Length: 0\r\n");
        dos.writeBytes("\r\n");
        dos.flush();
    }

    // 파일이나 매핑을 찾지 못했을 때 404 응답을 보냄
    public void notFound() throws IOException {
        byte[] body = "404 Not Found".getBytes(StandardCharsets.UTF_8);

        writeStatusLine(HttpStatus.NOT_FOUND);
        dos.writeBytes("Content-Type: text/plain;charset=utf-8\r\n");
        writeCookie();
        dos.writeBytes("Content-Length: " + body.length + "\r\n");
        dos.writeBytes("\r\n");
        dos.write(body);
        dos.flush();
    }

    private void writeStatusLine(HttpStatus status) throws IOException {
        dos.writeBytes("HTTP/1.1 " + status.getCode() + " " + status.getReasonPhrase() + "\r\n");
    }

    // 저장된 쿠키가 있으면 응답 헤더에 씀
    private void writeCookie() throws IOException {
        if (cookie != null) {
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
        }
    }
}
