package http.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequestUtils {
    // query parameter 처리하는 method
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
