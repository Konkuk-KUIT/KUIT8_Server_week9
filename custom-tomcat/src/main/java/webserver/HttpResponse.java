package webserver;

import http.ContentType;
import http.HttpHeader;
import http.HttpStatus;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    private final DataOutputStream dos;
    private final Map<HttpHeader, String> headers = new LinkedHashMap<>();

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }

    // 컨트롤러가 필요한 헤더를 추가
    public void addHeader(HttpHeader header, String value) {
        headers.put(header, value);
    }

    // 200 + 파일 내용
    public void forward(String url) throws IOException {
        Path path = Path.of("webapp" + url);
        if (!Files.exists(path) || Files.isDirectory(path)) {
            send(HttpStatus.NOT_FOUND, new byte[0]);
            return;
        }
        byte[] body = Files.readAllBytes(path);
        addHeader(HttpHeader.CONTENT_TYPE, ContentType.fromUrl(url).getMimeType());
        send(HttpStatus.OK, body);
    }

    // 302 + Location
    public void redirect(String location) throws IOException {
        addHeader(HttpHeader.LOCATION, location);
        send(HttpStatus.FOUND, new byte[0]);
    }

    public void notFound() throws IOException {
        send(HttpStatus.NOT_FOUND, new byte[0]);
    }

    public void methodNotAllowed() throws IOException {
        send(HttpStatus.METHOD_NOT_ALLOWED, new byte[0]);
    }

    // 실제로 쓰는 곳은 여기 한 군데
    private void send(HttpStatus status, byte[] body) throws IOException {
        addHeader(HttpHeader.CONTENT_LENGTH, String.valueOf(body.length));
        addHeader(HttpHeader.CONNECTION, "close");

        dos.writeBytes(status.toStatusLine());
        for (Map.Entry<HttpHeader, String> entry : headers.entrySet()) {
            dos.writeBytes(entry.getKey().line(entry.getValue()));
        }
        dos.writeBytes("\r\n");
        dos.write(body, 0, body.length);
        dos.flush();
    }
}