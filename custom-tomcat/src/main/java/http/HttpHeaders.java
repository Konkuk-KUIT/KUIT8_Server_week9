//이름 : 값 줄을 보관함.

package http;

import http.constant.HttpHeader;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class HttpHeaders {
    // 대소문자 구분 없이 찾을 수 있는 Map ("content-length"로 찾아도 "Content-Length"가 나옴)
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    // 1단계 while문을 이쪽으로 옮김
    public static HttpHeaders from(BufferedReader br) throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        while (true) {
            String line = br.readLine();
            if (line == null) {
                throw new EOFException("Request ended before the end of headers");
            }
            if (line.isEmpty()) {
                return httpHeaders;
            }
            String[] header = line.split(":", 2);
            String value = header.length == 2 ? header[1].trim() : "";
            httpHeaders.put(header[0].trim(), value);
        }
    }

    public void put(String name, String value) {
        headers.put(name, value);
    }

    public void put(HttpHeader header, String value) {
        put(header.getHeaderName(), value);
    }

    public String get(String name) {
        return headers.get(name);
    }

    public String get(HttpHeader header) {
        return get(header.getHeaderName());
    }

    public int getContentLength() {
        String value = get(HttpHeader.CONTENT_LENGTH);
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.trim());
    }

    public Iterable<Map.Entry<String, String>> entries() {
        return headers.entrySet();
    }
}
