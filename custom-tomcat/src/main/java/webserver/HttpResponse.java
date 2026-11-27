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
        String contentType = contentType(file);

        out.writeBytes(HttpStatus.OK.statusLine());
        out.writeBytes("Content-Type: " + contentType + "\r\n");
        out.writeBytes("Content-Length: " + body.length + "\r\n");
        out.writeBytes("Connection: close\r\n");
        out.writeBytes("\r\n");
        out.write(body);
        out.flush();
    }

    private String contentType(Path file) throws IOException {
        String name = file.getFileName().toString();
        if (name.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (name.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        String detectedType = Files.probeContentType(file);
        return detectedType == null ? "application/octet-stream" : detectedType;
    }

    public void notFound() throws IOException {
        out.writeBytes(HttpStatus.NOT_FOUND.statusLine());
        out.writeBytes("Content-Length: 0\r\n");
        out.writeBytes("Connection: close\r\n");
        out.writeBytes("\r\n");
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