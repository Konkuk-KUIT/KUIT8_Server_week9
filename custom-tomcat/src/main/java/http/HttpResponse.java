package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpResponse {
    private final DataOutputStream dos;

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(Path path) throws IOException {
        String contentType = path.getFileName().toString().endsWith(".css")
                ? "text/css"
                : "text/html";
        byte[] body = Files.readAllBytes(path);

        dos.writeBytes(HttpStatus.OK.statusLine());
        dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
        dos.writeBytes("Content-Length: " + body.length + "\r\n");
        dos.writeBytes("\r\n");
        dos.write(body);
        dos.flush();
    }

    public void redirect(String location) throws IOException {
        redirect(location, null);
    }

    public void redirect(String location, String cookie) throws IOException {
        dos.writeBytes(HttpStatus.FOUND.statusLine());
        dos.writeBytes("Location: " + location + "\r\n");

        if (cookie != null) {
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
        }

        dos.writeBytes("Content-Length: 0\r\n");
        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");
        dos.flush();
    }
}
