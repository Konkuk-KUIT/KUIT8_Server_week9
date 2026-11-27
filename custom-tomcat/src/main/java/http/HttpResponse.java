package http;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpResponse {
    private final DataOutputStream dos;

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(String path) throws IOException {
        byte[] body = Files.readAllBytes(Path.of("webapp" + path));
        String contentType = path.endsWith(".css") ? "text/css" : "text/html;charset=utf-8";
        dos.writeBytes("HTTP/1.1 " + HttpStatus.OK + "\r\n");
        dos.writeBytes("Content-Type: " + contentType + "\r\n");
        dos.writeBytes("Content-Length: " + body.length + "\r\n");
        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");
        dos.write(body, 0, body.length);
        dos.flush();
    }

    public void notFound() throws IOException {
        dos.writeBytes("HTTP/1.1 "+HttpStatus.NOT_FOUND+"\r\n");
        dos.writeBytes("Content-Length: 0\r\nConnection: close\r\n\r\n");
        dos.flush();
    }

    public void redirect(String location) throws IOException {
        redirect(location, null);
    }

    public void redirect(String location, String cookie) throws IOException {
        dos.writeBytes("HTTP/1.1 " + HttpStatus.FOUND + "\r\n");
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