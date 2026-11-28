package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    private final DataOutputStream output;

    public HttpResponse(OutputStream output) {
        this.output = new DataOutputStream(output);
    }

    // URL 경로에 해당하는 HTML 또는 CSS 파일을 응답합니다.
    public void forward(String path) throws IOException {
        if (!path.startsWith("/")) {
            throw new IOException("잘못된 요청 경로입니다.");
        }

        Path webRoot = Path.of("webapp").toAbsolutePath().normalize();
        Path file = webRoot.resolve(path.substring(1)).normalize();

        boolean supportedFile =
                path.endsWith(".html") || path.endsWith(".css");

        if (!file.startsWith(webRoot)
                || !supportedFile
                || !Files.isRegularFile(file)) {
            notFound();
            return;
        }

        String contentType = path.endsWith(".css")
                ? "text/css; charset=utf-8"
                : "text/html; charset=utf-8";

        byte[] body = Files.readAllBytes(file);

        send(
                HttpStatus.OK,
                Map.of("Content-Type", contentType),
                body
        );
    }

    public void redirect(String location) throws IOException {
        send(
                HttpStatus.FOUND,
                Map.of("Location", location),
                new byte[0]
        );
    }

    // 로그인 성공 시 Location과 Set-Cookie를 함께 전송합니다.
    public void redirect(String location, String cookie)
            throws IOException {

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Location", location);
        headers.put("Set-Cookie", cookie);

        send(HttpStatus.FOUND, headers, new byte[0]);
    }

    public void notFound() throws IOException {
        send(
                HttpStatus.NOT_FOUND,
                Map.of("Content-Type", "text/plain; charset=utf-8"),
                "Not Found".getBytes(StandardCharsets.UTF_8)
        );
    }

    private void send(
            HttpStatus status,
            Map<String, String> headers,
            byte[] body
    ) throws IOException {

        output.writeBytes(status.statusLine() + "\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            output.writeBytes(
                    header.getKey() + ": " + header.getValue() + "\r\n"
            );
        }

        output.writeBytes("Content-Length: " + body.length + "\r\n");
        output.writeBytes("Connection: close\r\n");
        output.writeBytes("\r\n");
        output.write(body);
        output.flush();
    }
}