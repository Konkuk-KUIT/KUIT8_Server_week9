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
    private final Map<String, String> parameters;

    private HttpRequest(HttpMethod method, String path,
                        Map<String, String> headers,
                        Map<String, String> parameters) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.parameters = parameters;
    }

    public static HttpRequest from(BufferedReader br) throws IOException {
        String requestLine = br.readLine();
        if (requestLine == null) {
            throw new IOException("Empty request");
        }

        String[] tokens = requestLine.split(" ");
        if (tokens.length < 3) {
            throw new IOException("Invalid request");
        }

        HttpMethod method = HttpMethod.valueOf(tokens[0]);
        String url = tokens[1];

        String[] urlParts = url.split("\\?", 2);
        String path = urlParts[0];

        Map<String, String> headers = new HashMap<>();
        String line;

        while ((line = br.readLine()) != null && !line.isEmpty()) {
            String[] header = line.split(":", 2);
            if (header.length == 2) {
                headers.put(
                        header[0].trim().toLowerCase(),
                        header[1].trim()
                );
            }
        }

        Map<String, String> parameters;

        if (method == HttpMethod.POST) {
            int length = Integer.parseInt(
                    headers.getOrDefault("content-length", "0")
            );
            String body = IOUtils.readData(br, length);
            parameters = HttpRequestUtils.parseQueryParameter(body);
        } else {
            String query = urlParts.length == 2 ? urlParts[1] : "";
            parameters = HttpRequestUtils.parseQueryParameter(query);
        }

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
        return headers.get(name.toLowerCase());
    }

    public boolean isLogined() {
        String cookie = getHeader("cookie");
        if (cookie == null) {
            return false;
        }

        for (String item : cookie.split(";")) {
            String[] pair = item.trim().split("=", 2);
            if (pair.length == 2
                    && pair[0].trim().equals("logined")
                    && pair[1].trim().equals("true")) {
                return true;
            }
        }

        return false;
    }
}