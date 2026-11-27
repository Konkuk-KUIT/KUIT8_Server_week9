package http;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class HttpRequest {
    private final HttpStartLine startLine;
    private final HttpHeaders headers;
    private final String body;

    private HttpRequest(HttpStartLine startLine, HttpHeaders headers, String body) {
        this.startLine = startLine;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest from(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null || line.isBlank()) {
            return null;
        }
        HttpStartLine startLine = HttpStartLine.from(line);
        HttpHeaders headers = HttpHeaders.from(reader);
        String body = "";
        if (startLine.getMethod() == HttpMethod.POST) {
            body = IOUtils.readData(reader, headers.getContentLength());
        }
        return new HttpRequest(startLine, headers, body);
    }

    public HttpMethod getMethod() {
        return startLine.getMethod();
    }

    public String getPath() {
        return startLine.getPath();
    }

    public String getVersion() {
        return startLine.getVersion();
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getParameters() {
        if (getMethod() == HttpMethod.POST) {
            return HttpRequestUtils.parseQueryParameter(body);
        }
        return HttpRequestUtils.parseQueryParameter(startLine.getQueryString());
    }

    public String getCookie(String name) {
        String cookieHeader = headers.get("Cookie");
        if (cookieHeader == null) {
            return null;
        }
        String value = null;
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && name.equals(parts[0].trim())) {
                value = parts[1].trim();
            }
        }
        return value;
    }
}
