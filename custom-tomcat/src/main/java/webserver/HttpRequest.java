package webserver;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final int contentLength;
    private final boolean isLogin;
    private final String path;
    private final Map<String, String> queryParameter;
    private final String requestBody;

    public HttpRequest(HttpMethod method, int contentLength, boolean isLogin, String path, Map<String, String> queryParameter, String requestBody) {
        this.method = method;
        this.contentLength = contentLength;
        this.isLogin = isLogin;
        this.path = path;
        this.queryParameter = queryParameter;
        this.requestBody = requestBody;
    }

    // 받은 요청에 대해 헤더, 쿼리, 본문을 분석해서 필드로 가진다
    public static HttpRequest from(BufferedReader br) throws IOException {
        List<List<String>> request = new ArrayList<>();
        String line;

        int contentLength = 0;
        boolean isLogin = false;
        while((line = br.readLine()) != null && !line.isEmpty()) {
            List<String> requestLine = Arrays.stream(line.split(" ")).toList();
            request.add(requestLine);

            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }

            if (line.toLowerCase().startsWith("cookie:")) {
                isLogin = Boolean.parseBoolean(line.split(":")[1].trim());
            }
        }

        List<String> startLine = request.getFirst();
        HttpMethod method = HttpMethod.valueOf(startLine.getFirst());
        List<String> endpoint = Arrays.stream(startLine.get(1).split("\\?")).toList();
        String path = endpoint.getFirst();
        Map<String, String> queryParameter = null;
        if (endpoint.size() >= 2) {
            queryParameter = HttpRequestUtils.parseQueryParameter(endpoint.getLast());
        }
        String requestBody = IOUtils.readData(br, contentLength);

        return new HttpRequest(method, contentLength, isLogin, path, queryParameter, requestBody);
    }

    public HttpMethod getHttpMethod() {
        return this.method;
    }

    public String getPath() {
        return path;
    }

    public int getContentLength() {
        return contentLength;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public Map<String, String> getQueryParameter() {
        return queryParameter;
    }

    public String getRequestBody() {
        return requestBody;
    }
}
