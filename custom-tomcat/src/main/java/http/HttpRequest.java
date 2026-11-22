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
    private final Map<String, String> headers;
    private final Map<String, String> params;

    private HttpRequest(HttpMethod method, String path, Map<String, String> headers, Map<String, String> params) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.params = params;
    }

    public static HttpRequest from(BufferedReader br) throws IOException {
        String requestLine = br.readLine();
        if (requestLine == null) {
            return null;
        }
        String[] tokens = requestLine.split(" ");
        HttpMethod method = HttpMethod.valueOf(tokens[0]);

        String path = tokens[1];
        String query = "";
        int queryStart = path.indexOf('?');
        if (queryStart != -1) {
            query = path.substring(queryStart + 1);
            path = path.substring(0, queryStart);
        }

        Map<String, String> headers = new HashMap<>();
        String line = br.readLine();
        while (line != null && !line.isEmpty()) {
            String[] header = line.split(":", 2);
            headers.put(header[0].trim().toLowerCase(), header[1].trim());
            line = br.readLine();
        }

        Map<String, String> params;
        if (method == HttpMethod.POST) {
            int contentLength = Integer.parseInt(headers.getOrDefault("content-length", "0"));
            params = HttpRequestUtils.parseQueryParameter(IOUtils.readData(br, contentLength));
        } else {
            params = HttpRequestUtils.parseQueryParameter(query);
        }
        return new HttpRequest(method, path, headers, params);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String name) {
        return headers.getOrDefault(name.toLowerCase(), "");
    }

    public String getParameter(String name) {
        return params.get(name);
    }
}