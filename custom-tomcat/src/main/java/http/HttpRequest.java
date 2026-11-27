package http;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> parameters;
    private final Map<String, String> headers;

    private HttpRequest(
            HttpMethod method,
            String path,
            Map<String, String> parameters,
            Map<String, String> headers
    ) {
        this.method = method;
        this.path = path;
        this.parameters = parameters;
        this.headers = headers;
    }

    public static HttpRequest from(BufferedReader br) throws IOException {
        // 1. 요청 첫 줄 읽기
        String requestLine = br.readLine();
        String[] tokens = requestLine.split(" ");

        HttpMethod method = HttpMethod.valueOf(tokens[0]);

        // 2. 경로와 쿼리 분리
        String[] url = tokens[1].split("\\?", 2);
        String path = url[0];
        String query = url.length == 2 ? url[1] : "";

        // 3. 빈 줄까지 헤더 읽기
        Map<String, String> headers = new HashMap<>();
        String line;

        while ((line = br.readLine()) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');

            if (colon > 0) {
                //이때 header는 브라우저가 임의로 대소문자 상관없이 보낼 수 있으므로 toLowerCase를 하든 구분을 없애줘야함
                String name = line.substring(0, colon).trim().toLowerCase();
                String value = line.substring(colon + 1).trim();

                headers.put(name, value);
            }
        }

        // 4. GET은 쿼리, POST는 본문에서 입력값 읽기
        String data = query;

        if (method == HttpMethod.POST) {
            int contentLength = Integer.parseInt(
                headers.getOrDefault("content-length", "0")
            );

            data = IOUtils.readData(br, contentLength);
        }

        Map<String, String> parameters =
            HttpRequestUtils.parseQueryParameter(data);

        // 5. 분석한 값들을 담아서 반환
        return new HttpRequest(method, path, parameters, headers);
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

    public String getCookie(String name) {
        String cookieHeader = headers.getOrDefault("cookie", "");

        for (String item : cookieHeader.split(";")) {
            String[] pair = item.trim().split("=", 2);

            if (pair.length == 2 && pair[0].equals(name)) {
                return pair[1];
            }
        }

        return null;
    }
}