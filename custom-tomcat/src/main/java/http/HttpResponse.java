
package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpResponse {

    private final DataOutputStream dos;

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    // HTML, CSS 등 정적 파일 반환
    public void forward(String path) throws IOException {

        Path webappRoot = Paths.get("webapp")
                .toAbsolutePath()
                .normalize();

        Path filePath = webappRoot
                .resolve(path.substring(1))
                .normalize();

        System.out.println("File Path: " + filePath);

        if (!filePath.startsWith(webappRoot)) {
            error(HttpStatus.FORBIDDEN);
            return;
        }

        if (!Files.isRegularFile(filePath)) {
            error(HttpStatus.NOT_FOUND);
            return;
        }

        byte[] body = Files.readAllBytes(filePath);

        String contentType;

        if (path.endsWith(".html")) {
            contentType = "text/html; charset=utf-8";
        } else if (path.endsWith(".css")) {
            contentType = "text/css; charset=utf-8";
        } else {
            contentType = "application/octet-stream";
        }

        response200Header(body.length, contentType);
        responseBody(body);
    }

    // 302 리다이렉트
    public void redirect(String path) throws IOException {

        dos.writeBytes("HTTP/1.1 "
                + HttpStatus.FOUND.getCode() + " "
                + HttpStatus.FOUND.getMessage() + "\r\n");
        dos.writeBytes("Location: " + path + "\r\n");
        dos.writeBytes("Content-Length: 0\r\n");
        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");
        dos.flush();
    }

    // 로그인 성공 시 쿠키를 포함하는 302 응답
    public void redirect(String path, String cookie) throws IOException {

        dos.writeBytes("HTTP/1.1 "
                + HttpStatus.FOUND.getCode() + " "
                + HttpStatus.FOUND.getMessage() + "\r\n");
        dos.writeBytes("Location: " + path + "\r\n");
        dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
        dos.writeBytes("Content-Length: 0\r\n");
        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");
        dos.flush();
    }

    // 오류 응답
    public void error(HttpStatus status) throws IOException {

        byte[] body = (status.getCode() + " " + status.getMessage())
                .getBytes(StandardCharsets.UTF_8);

        dos.writeBytes("HTTP/1.1 "
                + status.getCode() + " "
                + status.getMessage() + "\r\n");
        dos.writeBytes("Content-Type: text/plain; charset=utf-8\r\n");
        dos.writeBytes("Content-Length: " + body.length + "\r\n");
        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");

        responseBody(body);
    }

    private void response200Header(
            int lengthOfBodyContent,
            String contentType) throws IOException {

        dos.writeBytes("HTTP/1.1 "
                + HttpStatus.OK.getCode() + " "
                + HttpStatus.OK.getMessage() + "\r\n");
        dos.writeBytes("Content-Type: " + contentType + "\r\n");
        dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");
    }

    private void responseBody(byte[] body) throws IOException {

        dos.write(body);
        dos.flush();
    }
}
