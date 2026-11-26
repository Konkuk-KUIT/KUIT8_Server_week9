package http;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public record HttpRequest(
        HttpStartLine startLine,
        HttpHeader headers,
        String body
) {
    public static HttpRequest from(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        HttpStartLine startLine = HttpStartLine.parse(line);

        if (startLine == null) {
            return null;
        }

        HttpHeader headers = HttpHeader.readFrom(reader);
        int contentLength = headers.contentLength();
        String body = IOUtils.readData(reader, contentLength);

        return new HttpRequest(startLine, headers, body);
    }

    public Map<String, String> formParams() {
        return HttpRequestUtils.parseQueryParameter(body);
    }
}
