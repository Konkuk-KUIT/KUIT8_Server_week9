//HTTP 요청 첫 줄을 메서드 / 경로 / 쿼리 / 버전으로 쪼개서 보관함.

package http.request;

import http.constant.HttpMethod;

public class HttpStartLine {
    private final HttpMethod method;
    private final String path;
    private final String queryString;
    private final String version;

    private HttpStartLine(HttpMethod method, String path, String queryString, String version) {
        this.method = method;
        this.path = path;
        this.queryString = queryString;
        this.version = version;
    }

    // "GET /user/signup?userId=kim HTTP/1.1" 한 줄을 받아서 조각냄
    public static HttpStartLine from(String line) {
        String[] tokens = line.split(" ");
        if (tokens.length != 3) {
            throw new IllegalArgumentException("Invalid start line: " + line);
        }
        HttpMethod method = HttpMethod.valueOf(tokens[0]);   // "GET" → HttpMethod.GET
        String url = tokens[1];
        String version = tokens[2];

        String path = url;
        String queryString = "";
        int questionMark = url.indexOf('?');
        if (questionMark != -1) {
            path = url.substring(0, questionMark);
            queryString = url.substring(questionMark + 1);
        }
        return new HttpStartLine(method, path, queryString, version);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getVersion() {
        return version;
    }
}
