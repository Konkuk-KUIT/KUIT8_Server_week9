package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpResponse {
    private final DataOutputStream out;

    public HttpResponse(OutputStream out) {
        this.out = new DataOutputStream(out);
    }

    public void forward(Path file) throws IOException {
        byte[] body = Files.readAllBytes(file);
        String contentType = file.getFileName().toString().endsWith(".css")
                ? "text/css"
                : "text/html";

        out.writeBytes(HttpStatus.OK.statusLine());
        out.writeBytes("Content-Type: " + contentType + "; charset=UTF-8\r\n");
        out.writeBytes("Content-Length: " + body.length + "\r\n");
        out.writeBytes("Connection: close\r\n");
        out.writeBytes("\r\n");
        out.write(body);
        out.flush();
    }

    public void redirect(String path) throws IOException {
        redirect(path, null);
    }

    public void redirect(String path, String cookie) throws IOException {
        out.writeBytes(HttpStatus.FOUND.statusLine());
        out.writeBytes("Location: " + path + "\r\n");
        if (cookie != null) {
            out.writeBytes("Set-Cookie: " + cookie + "\r\n");
        }
        out.writeBytes("Content-Length: 0\r\n");
        out.writeBytes("Connection: close\r\n");
        out.writeBytes("\r\n");
        out.flush();
    }
}