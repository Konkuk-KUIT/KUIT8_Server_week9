
package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

public class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final String version;
    private final String queryString;
    private final Map<String, String> headers;
    private final String body;
    private final int contentLength;

    private HttpRequest(
            HttpMethod method,
            String path,
            String version,
            String queryString,
            Map<String, String> headers,
            String body,
            int contentLength) {

        this.method = method;
        this.path = path;
        this.version = version;
        this.queryString = queryString;
        this.headers = Map.copyOf(headers);
        this.body = body;
        this.contentLength = contentLength;
    }

    public static HttpRequest from(BufferedReader br) throws IOException {

        // 1. HTTP 요청 첫 번째 줄 읽기
        String requestLine = br.readLine();

        if (requestLine == null || requestLine.isBlank()) {
            return null;
        }

        System.out.println("HTTP Request: " + requestLine);

        // 2. HTTP Method와 URL 추출
        String[] tokens = requestLine.trim().split("\\s+");

        if (tokens.length != 3) {
            throw new IllegalArgumentException("Invalid HTTP request line");
        }

        HttpMethod method;

        try {
            method = HttpMethod.valueOf(tokens[0]);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("Unsupported HTTP method");
        }

        String url = tokens[1];
        String version = tokens[2];

        // 3. HTTP Header 끝까지 읽기
        Map<String, String> headers = new HashMap<>();
        String line;

        while ((line = br.readLine()) != null && !line.isEmpty()) {

            int colonIndex = line.indexOf(":");

            if (colonIndex < 0) {
                throw new IllegalArgumentException("Invalid HTTP header");
            }

            String name = line.substring(0, colonIndex)
                    .trim()
                    .toLowerCase(Locale.ROOT);

            String value = line.substring(colonIndex + 1).trim();

            if (name.isEmpty()) {
                throw new IllegalArgumentException("Invalid HTTP header");
            }

            headers.put(name, value);
        }

        if (line == null) {
            return null;
        }

        int contentLength = -1;

        if (headers.containsKey("content-length")) {
            contentLength = Integer.parseInt(headers.get("content-length"));

            if (contentLength < 0) {
                throw new IllegalArgumentException("Invalid Content-Length");
            }
        }

        // 4. POST 요청 본문 읽기
        String body = null;

        if (method == HttpMethod.POST && contentLength >= 0) {
            body = IOUtils.readData(br, contentLength);
        }

        // 5. URL의 쿼리 문자열 제거
        int queryIndex = url.indexOf("?");
        String queryString = null;

        if (queryIndex != -1) {
            queryString = url.substring(queryIndex + 1);
            url = url.substring(0, queryIndex);
        }

        return new HttpRequest(
                method,
                url,
                version,
                queryString,
                headers,
                body,
                contentLength
        );
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
        return headers.get(name.toLowerCase(Locale.ROOT));
    }

    public String getBody() {
        return body;
    }

    public int getContentLength() {
        return contentLength;
    }

    public Map<String, String> getParameters() {

        if (method == HttpMethod.POST) {
            return HttpRequestUtils.parseQueryParameter(body);
        }

        return HttpRequestUtils.parseQueryParameter(queryString);
    }
}
