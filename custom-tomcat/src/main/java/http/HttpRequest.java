package http;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> parameters;

    private HttpRequest(
            HttpMethod method,
            String path,
            Map<String, String> headers,
            Map<String, String> parameters
    ) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.parameters = parameters;
    }

    public static HttpRequest from(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isBlank()) {
            throw new IllegalArgumentException("오류(첫줄x)");
        }

        String[] tokens = requestLine.split(" ");

        if (tokens.length != 3) {
            throw new IllegalArgumentException("첫줄이 잘못됨");
        }

        HttpMethod method = HttpMethod.valueOf(tokens[0]);

        String[] urlParts = tokens[1].split("\\?", 2);
        String path = urlParts[0];
        String query = urlParts.length > 1 ? urlParts[1] : "";

        Map<String, String> headers = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');

            if (colonIndex < 0) {
                throw new IllegalArgumentException("잘못된 헤더");
            }

            String name = line.substring(0, colonIndex)
                    .trim()
                    .toLowerCase(Locale.ROOT);

            String value = line.substring(colonIndex + 1).trim();
            headers.put(name, value);
        }

        if (line == null) {
            throw new IOException("헤더를 읽는 중 연결 종료됨.");
        }

        String formData = query;

        if (method == HttpMethod.POST) {
            int contentLength = Integer.parseInt(
                    headers.getOrDefault("content-length", "0")
            );

            formData = IOUtils.readData(reader, contentLength);
        }

        Map<String, String> parameters =
                HttpRequestUtils.parseQueryParameter(formData);

        return new HttpRequest(method, path, headers, parameters);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean isLoggedIn() {
        String cookieHeader = getHeader("Cookie");

        if (cookieHeader == null) {
            return false;
        }

        for (String cookie : cookieHeader.split(";")) {
            String[] pair = cookie.trim().split("=", 2);

            if (pair.length == 2
                    && pair[0].trim().equals("logined")
                    && pair[1].trim().equals("true")) {
                return true;
            }
        }

        return false;
    }
}
