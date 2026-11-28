package http;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, String> query;
    private final Map<String, String> form;

    private HttpRequest(
            HttpMethod method,
            String path,
            String version,
            Map<String, String> headers,
            Map<String, String> query,
            Map<String, String> form
    ) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.query = Map.copyOf(query);
        this.form = Map.copyOf(form);
    }

    // Reader로부터 요청을 분석해서 HttpRequest 객체를 만듭니다.
    public static HttpRequest from(BufferedReader reader)
            throws IOException {

        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] startLine = requestLine.split(" ", 3);

        if (startLine.length != 3) {
            throw new IOException("잘못된 요청 시작 줄입니다.");
        }

        HttpMethod method = HttpMethod.valueOf(startLine[0]);
        String version = startLine[2];

        String[] requestTarget = startLine[1].split("\\?", 2);
        String path = requestTarget[0];
        String queryString = "";

        if (requestTarget.length == 2) {
            queryString = requestTarget[1];
        }

        Map<String, String> headers =
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        String line;

        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }

            String[] header = line.split(":", 2);

            if (header.length != 2) {
                throw new IOException("잘못된 요청 헤더입니다.");
            }

            headers.put(header[0].trim(), header[1].trim());
        }

        if (line == null) {
            return null;
        }

        String body = "";

        if (method == HttpMethod.POST) {
            int contentLength = Integer.parseInt(
                    headers.getOrDefault("Content-Length", "0")
            );

            // 헤더를 읽은 동일한 Reader를 사용합니다.
            body = IOUtils.readData(reader, contentLength);
        }

        return new HttpRequest(
                method,
                path,
                version,
                headers,
                HttpRequestUtils.parseQueryParameter(queryString),
                HttpRequestUtils.parseQueryParameter(body)
        );
    }

    public HttpMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    public String version() {
        return version;
    }

    public String header(String name) {
        return headers.get(name);
    }

    public Map<String, String> query() {
        return query;
    }

    public Map<String, String> form() {
        return form;
    }

    public boolean isLoggedIn() {
        String cookieHeader = header("Cookie");

        if (cookieHeader == null) {
            return false;
        }

        for (String cookie : cookieHeader.split(";")) {
            String[] pair = cookie.trim().split("=", 2);

            if (pair.length == 2
                    && pair[0].equals("logined")
                    && pair[1].equals("true")) {
                return true;
            }
        }

        return false;
    }
}