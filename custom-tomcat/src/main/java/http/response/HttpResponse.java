//응답을 작성함.

package http.response;

import http.HttpHeaders;
import http.constant.ContentType;
import http.constant.HttpHeader;
import http.constant.HttpStatus;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class HttpResponse {
    private static final String WEBAPP = "webapp";

    private final DataOutputStream dos;
    private final HttpHeaders headers = new HttpHeaders();   // 헤더 보관함 재활용!

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void addHeader(HttpHeader header, String value) {
        headers.put(header, value);
    }

    // 파일 내용을 200 OK로 돌려준다 (1단계 ⑤)
    public void forward(String path) throws IOException {
        Path filePath = Paths.get(WEBAPP + path);
        if (!Files.isRegularFile(filePath)) {
            notFound();
            return;
        }
        byte[] body = Files.readAllBytes(filePath);
        addHeader(HttpHeader.CONTENT_TYPE, ContentType.from(path).getValue());
        addHeader(HttpHeader.CONTENT_LENGTH, String.valueOf(body.length));
        write(HttpStatus.OK, body);
    }

    // 302로 path으로 다시 보냄 (1단계 response302Header)
    public void redirect(String path) throws IOException {
        addHeader(HttpHeader.LOCATION, path);
        addHeader(HttpHeader.CONTENT_LENGTH, "0");
        write(HttpStatus.FOUND, new byte[0]);
    }

    public void notFound() throws IOException {
        addHeader(HttpHeader.CONTENT_LENGTH, "0");
        write(HttpStatus.NOT_FOUND, new byte[0]);
    }

    // 상태 줄 → 헤더들 → 빈 줄 → 본문 순서로 실제로 쓰는 곳
    private void write(HttpStatus status, byte[] body) throws IOException {
        addHeader(HttpHeader.CONNECTION, "close");
        dos.writeBytes("HTTP/1.1 " + status.getCode() + " " + status.getMessage() + "\r\n");
        for (Map.Entry<String, String> header : headers.entries()) {
            dos.writeBytes(header.getKey() + ": " + header.getValue() + "\r\n");
        }
        dos.writeBytes("\r\n");
        dos.write(body);
        dos.flush();
    }
}