package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpHeader {
    private final Map<String, String> values;

    private HttpHeader(Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    public static HttpHeader readFrom(BufferedReader reader) throws IOException {
        Map<String, String> values = new HashMap<>();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                return new HttpHeader(values);
            }

            String[] parts = line.split(":", 2);
            if (parts.length != 2 || parts[0].isEmpty()) {
                throw new IOException();
            }

            String name = parts[0].toLowerCase(Locale.ROOT);
            values.put(name, parts[1].trim());
        }

        throw new IOException();
    }

    public String get(String name) {
        return values.get(name.toLowerCase(Locale.ROOT));
    }

    public int contentLength() throws IOException {
        String value = get("Content-Length");
        if (value == null) {
            return 0;
        }

        try {
            int length = Integer.parseInt(value);
            if (length < 0) {
                throw new IOException();
            }
            return length;
        } catch (NumberFormatException e) {
            throw new IOException("Invalid Content-Length", e);
        }
    }
}
