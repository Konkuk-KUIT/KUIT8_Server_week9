package support;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

// 테스트에서 응답의 헤더와 본문을 구분해 확인하기 위한 보조 코드입니다.
public record HttpTestResponse(String statusLine, Map<String, String> headers, byte[] body) {
    public static HttpTestResponse parse(byte[] bytes) {
        for (int i = 0; i < bytes.length - 3; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n'
                    && bytes[i + 2] == '\r' && bytes[i + 3] == '\n') {
                String[] lines = new String(bytes, 0, i, StandardCharsets.ISO_8859_1).split("\r\n");
                Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                for (int j = 1; j < lines.length; j++) {
                    String[] header = lines[j].split(":", 2);
                    headers.put(header[0].trim(), header[1].trim());
                }
                return new HttpTestResponse(lines[0], headers, Arrays.copyOfRange(bytes, i + 4, bytes.length));
            }
        }
        throw new AssertionError("응답에 헤더와 본문을 구분하는 빈 줄이 없습니다.");
    }

    public String header(String name) {
        return headers.get(name);
    }
}
