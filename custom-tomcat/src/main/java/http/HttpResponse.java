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
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "html", "text/html;charset=utf-8",
            "css", "text/css",
            "js", "text/javascript;charset=utf-8",
            "png", "image/png",
            "jpeg", "image/jpeg",
            "jpg", "image/jpeg"
    );
    private final DataOutputStream output;
    private final Path webRoot = Path.of("webapp").toAbsolutePath().normalize();

    public HttpResponse(OutputStream output) {
        this.output = new DataOutputStream(output);
    }

    // 파일의 실제 내용을 응답합니다. 브라우저 주소는 변경하지 않습니다.
    public void forward(String path) throws IOException {
        if ("/".equals(path)) {
            path = "/index.html";
        }
        if (!path.startsWith("/")) {
            notFound();
            return;
        }
        Path relativePath = Path.of(path.substring(1)).normalize();
        // 요청에는 webapp 안의 상대 경로만 허용합니다.
        if (relativePath.getRoot() != null || relativePath.startsWith("..")) {
            notFound();
            return;
        }
        Path filePath = webRoot.resolve(relativePath).normalize();
        if (!filePath.startsWith(webRoot) || !Files.isRegularFile(filePath)) {
            notFound();
            return;
        }

        byte[] body = Files.readAllBytes(filePath);
        String fileName = filePath.getFileName().toString();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(java.util.Locale.ROOT);
        String contentType = CONTENT_TYPES.getOrDefault(extension, "application/octet-stream");
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", contentType);
        write(HttpStatus.OK, headers, body);
    }

    public void redirect(String path) throws IOException {
        redirect(path, null);
    }

    // 로그인 성공 시에는 이동할 주소와 쿠키를 함께 보냅니다.
    public void redirect(String path, String cookie) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Location", path);
        if (cookie != null) {
            headers.put("Set-Cookie", cookie);
        }
        write(HttpStatus.FOUND, headers, new byte[0]);
    }

    public void notFound() throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "text/html;charset=utf-8");
        write(HttpStatus.NOT_FOUND, headers, "Not Found".getBytes(StandardCharsets.UTF_8));
    }

    // 상태 줄 -> 헤더 -> 빈 줄 -> 본문 순서로 실제 응답을 전송합니다.
    private void write(HttpStatus status, Map<String, String> headers, byte[] body) throws IOException {
        output.writeBytes("HTTP/1.1 " + status.getStatusLine() + "\r\n");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            output.writeBytes(header.getKey() + ": " + header.getValue() + "\r\n");
        }
        output.writeBytes("Content-Length: " + body.length + "\r\n");
        output.writeBytes("Connection: close\r\n");
        output.writeBytes("\r\n");
        output.write(body);
        output.flush();
    }
}
