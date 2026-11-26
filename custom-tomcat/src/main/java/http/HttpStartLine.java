package http;

import java.io.IOException;

public record HttpStartLine(
        HttpMethod method, String path, String version, String query
) {
    public static HttpStartLine parse(String line) {
        if (line == null) {
            return null;
        }

        String[] parts = line.split(" ", -1);
        if (parts.length != 3 || parts[1].isEmpty()
                || !parts[2].startsWith("HTTP/")) {
            return null;
        }

        HttpMethod method = HttpMethod.from(parts[0]);
        if (method == null) {
            return null;
        }

        String[] target = parts[1].split("\\?", 2);
        String query = target.length == 2 ? target[1] : "";

        return new HttpStartLine(method, target[0], parts[2], query);
    }
}
