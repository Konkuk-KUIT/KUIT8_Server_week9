package http;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class HttpRequest {

    //request는 요청마다 별도의 객체. 아래 5개 변수는 하나의 HTTP 리퀘스트를 표현함
    private final HttpMethod method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, String> parameters;

    // 외부에서는 from()을 통해 생성
    private HttpRequest(HttpMethod method, String path, String version,
                        Map<String, String> headers,
                        Map<String, String> parameters) {

        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = Collections.unmodifiableMap(headers);
        this.parameters = Map.copyOf(parameters);
    }

    public static HttpRequest from(BufferedReader reader)
            throws IOException {

        // 1. 시작 줄 분석: GET /index.html HTTP/1.1
        String firstLine = reader.readLine();

        if (firstLine == null) return null;

        String[] parts = firstLine.split(" ", 3);

        if (parts.length != 3 || !parts[1].startsWith("/") || !parts[2].startsWith("HTTP/")) {
            throw new IllegalArgumentException("잘못된 HTTP 시작 줄");
        }

        HttpMethod method = HttpMethod.from(parts[0]);
        String version = parts[2];

        // 2. URL과 쿼리 문자열 분리
        String uri = parts[1];
        int index = uri.indexOf('?');

        String path = index == -1
                ? uri
                : uri.substring(0, index);

        String query = index == -1
                ? ""
                : uri.substring(index + 1);

        // 3. 헤더 읽기 (이름은 대소문자 구분 없이 저장. 그래서 TreeMap 사용)
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {

            int colon = line.indexOf(':');

            if (colon <= 0) {
                throw new IllegalArgumentException("잘못된 HTTP 헤더");
            }

            headers.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim()
            );
        }

        if (line == null) {
            throw new EOFException("HTTP 헤더가 끝나지 않음");
        }

        // 4. GET은 URL, POST는 본문에서 파라미터 읽기
        String encodedData = "";

        if (method == HttpMethod.GET) {
            encodedData = query;

        } else if (method == HttpMethod.POST) {

            String length = headers.get("Content-Length");

            if (length == null) {
                throw new IllegalArgumentException("Content-Length 누락");
            }

            encodedData = IOUtils.readData(reader, Integer.parseInt(length));
        }

        Map<String, String> parameters = HttpRequestUtils.parseQueryParameter(encodedData);

        return new HttpRequest(method, path, version, headers, parameters);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }
}

