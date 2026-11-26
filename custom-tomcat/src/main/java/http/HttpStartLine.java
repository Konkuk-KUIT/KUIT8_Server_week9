package http;

public class HttpStartLine {
    private final HttpMethod method;
    private final String path;
    private final String version;
    private final String queryString;

    private HttpStartLine(HttpMethod method, String path, String version, String queryString) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.queryString = queryString;
    }

    public static HttpStartLine from(String line) {
        // 예: POST /user/signup HTTP/1.1
        String[] parts = line.split(" ", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("HTTP 요청 첫 줄이 올바르지 않습니다.");
        }
        String[] urlParts = parts[1].split("\\?", 2);
        String queryString = "";
        if (urlParts.length == 2) {
            queryString = urlParts[1];
        }
        return new HttpStartLine(HttpMethod.from(parts[0]), urlParts[0], parts[2], queryString);
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

    public String getQueryString() {
        return queryString;
    }
}
