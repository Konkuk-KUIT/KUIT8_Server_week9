package http.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequestUtils {
    /**
     * Parses a query string (without '?') or an application/x-www-form-urlencoded body.
     * Decode only after splitting: encoded '&' and '=' may be part of a value.
     * Null/empty input produces an empty map; missing '=' means an empty value.
     * Empty segments are ignored. Empty keys, duplicate decoded keys and malformed
     * percent escapes are rejected with IllegalArgumentException.
     */
    public static Map<String, String> parseQueryParameter(String queryString) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return parameters;
        }

        for (String parameter : queryString.split("&")) {
            if (parameter.isEmpty()) {
                continue;
            }
            String[] pair = parameter.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length == 2 
                    ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            if (key.isEmpty()) {
                throw new IllegalArgumentException("Parameter names must not be empty");
            }
            if (parameters.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate parameter names are not supported");
            }
            parameters.put(key, value);
        }
        return parameters;
    }
}
