package webserver;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public record HttpRequest(
        HttpMethod method,
        String path,
        String version,
        Map<String, String> headers,
        Map<String, String> form
) {
    public static HttpRequest from(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null) {
            return null;
        }

        String[] parts = requestLine.split(" ", 3);
        if (parts.length != 3) {
            return null;
        }

        HttpMethod method = switch (parts[0]) {
            case "GET" -> HttpMethod.GET;
            case "POST" -> HttpMethod.POST;
            default -> null;
        };
        if (method == null) {
            return null;
        }

        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] fields = line.split(":", 2);
            if (fields.length == 2) {
                headers.merge(fields[0].trim(), fields[1].trim(),
                        (previous, current) -> previous + "; " + current);
            }
        }
        if (line == null) {
            return null;
        }

        Map<String, String> form = Map.of();
        if (method == HttpMethod.POST) {
            try {
                int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
                if (contentLength <= 0) {
                    return null;
                }

                String body = IOUtils.readData(reader, contentLength);
                form = HttpRequestUtils.parseQueryParameter(body);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return new HttpRequest(method, parts[1], parts[2],
                Collections.unmodifiableMap(headers), Map.copyOf(form));
    }

    public String header(String name) {
        return headers.get(name);
    }
}