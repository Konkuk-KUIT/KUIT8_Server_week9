package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpResponse {
    private final DataOutputStream dos;

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }

    public void forward(String path) throws IOException {
        byte[] body = Files.readAllBytes(Path.of("webapp" + path));

        String contentType = path.endsWith(".css")
                ? "text/css"
                : "text/html;charset=utf-8";

        dos.writeBytes("HTTP/1.1 " + HttpStatus.OK.getValue() + "\r\n");
        dos.writeBytes("Content-Type: " + contentType + "\r\n");
        dos.writeBytes("Content-Length: " + body.length + "\r\n");
        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");
        dos.write(body);
        dos.flush();
    }

    public void redirect(String path) throws IOException {
        redirect(path, null);
    }

    public void redirect(String path, String cookie) throws IOException {
        dos.writeBytes("HTTP/1.1 " + HttpStatus.FOUND.getValue() + "\r\n");
        dos.writeBytes("Location: " + path + "\r\n");

        if (cookie != null) {
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
        }

        dos.writeBytes("Content-Length: 0\r\n");
        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");
        dos.flush();
    }
}