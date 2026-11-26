package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {

    private final DataOutputStream out;
    private final Path webRoot;

    public HttpResponse(OutputStream outputStream) {
        this(outputStream, Paths.get("webapp"));
    }

    // 테스트에서 임시 디렉터리를 사용할 수 있도록 추가
    public HttpResponse(OutputStream outputStream, Path webRoot) {
        this.out = new DataOutputStream(outputStream);
        this.webRoot = webRoot.toAbsolutePath().normalize();
    }

    // HTML, CSS, JavaScript 파일 응답
    public void forward(String requestPath) throws IOException {

        String path = requestPath.equals("/")
                ? "/index.html"
                : requestPath;

        String contentType = findContentType(path);

        if (contentType == null || !path.startsWith("/")) {
            error(HttpStatus.NOT_FOUND);
            return;
        }

        Path file = webRoot.resolve(path.substring(1)).normalize();

        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            error(HttpStatus.NOT_FOUND);
            return;
        }

        byte[] body = Files.readAllBytes(file);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", contentType);

        send(HttpStatus.OK, headers, body);
    }

    // 일반 리다이렉트
    public void redirect(String location) throws IOException {
        redirect(location, Map.of());
    }

    // 추가 헤더를 전달하는 리다이렉트
    public void redirect(String location, Map<String, String> extraHeaders) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Location", location);
        headers.putAll(extraHeaders);

        send(HttpStatus.FOUND, headers, new byte[0]);
    }

    // 오류 응답
    public void error(HttpStatus status) throws IOException {
        send(status, Map.of(), new byte[0]);
    }

    // 확장자로 Content-Type 결정
    private String findContentType(String path) {

        if (path.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (path.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (path.endsWith(".js")) {
            return "text/javascript; charset=UTF-8";
        }

        return null;
    }

    // 모든 응답이 공통으로 사용하는 전송 메서드
    private void send(HttpStatus status, Map<String, String> headers, byte[] body) throws IOException {
        out.writeBytes(status.getStatusLine() + "\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            out.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }

        out.writeBytes("Content-Length: " + body.length + "\r\n");

        out.writeBytes("Connection: close\r\n");

        // 헤더와 본문 구분
        out.writeBytes("\r\n");

        // 실제 본문 전송
        out.write(body);
        out.flush();
    }
}

