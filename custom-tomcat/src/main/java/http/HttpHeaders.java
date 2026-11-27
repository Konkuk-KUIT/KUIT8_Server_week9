package http;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class HttpHeaders {
    private final Map<String, String> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private HttpHeaders() {
    }

    public static HttpHeaders from(BufferedReader reader) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                return headers;
            }
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String name = parts[0].trim();
                String value = parts[1].trim();
                if ("Cookie".equalsIgnoreCase(name) && headers.get(name) != null) {
                    value = headers.get(name) + "; " + value;
                }
                headers.values.put(name, value);
            }
        }
        throw new EOFException("HTTP 요청 헤더가 빈 줄 전에 끝났습니다.");
    }

    public String get(String name) {
        return values.get(name);
    }

    public int getContentLength() {
        String value = get("Content-Length");
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value);
    }
}
