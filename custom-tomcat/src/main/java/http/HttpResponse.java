package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {

    private static final Path WEB_ROOT =
            Path.of("webapp").toAbsolutePath().normalize();

    private final DataOutputStream out;
    private final Map<String, String> headers = new LinkedHashMap<>();

    public HttpResponse(DataOutputStream out) {
        this.out = out;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void forward(String path) throws IOException {
        String requestedPath = path.equals("/") ? "/index.html" : path;

        Path filePath = WEB_ROOT.resolve(
                requestedPath.startsWith("/")
                        ? requestedPath.substring(1)
                        : requestedPath
        ).normalize();


        byte[] body = Files.readAllBytes(filePath);

        if (requestedPath.endsWith(".css")) {
            addHeader("Content-Type", "text/css; charset=utf-8");
        } else {
            addHeader("Content-Type", "text/html; charset=utf-8");
        }

        send(HttpStatus.OK, body);
    }

    public void redirect(String path) throws IOException {
        addHeader("Location", path);
        send(HttpStatus.FOUND, new byte[0]);
    }

    public void sendText(HttpStatus status, String text) throws IOException {
        addHeader("Content-Type", "text/plain; charset=utf-8");
        send(status, text.getBytes(StandardCharsets.UTF_8));
    }

    private void send(HttpStatus status, byte[] body) throws IOException {
        addHeader("Content-Length", String.valueOf(body.length));
        addHeader("Connection", "close");

        out.writeBytes(status.statusLine() + "\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            out.writeBytes(header.getKey() + ": " + header.getValue() + "\r\n");
        }

        out.writeBytes("\r\n");
        out.write(body);
        out.flush();
    }
}