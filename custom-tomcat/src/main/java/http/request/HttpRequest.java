//요청 전체 담당

package http.request;

import http.HttpHeaders;
import http.constant.HttpHeader;
import http.constant.HttpMethod;
import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.Map;

public class HttpRequest {
    private final HttpStartLine startLine;      // 첫 줄
    private final HttpHeaders headers;          // 헤더들
    private final Map<String, String> parameters;  // userId, password 같은 값들

    private HttpRequest(HttpStartLine startLine, HttpHeaders headers, Map<String, String> parameters) {
        this.startLine = startLine;
        this.headers = headers;
        this.parameters = parameters;
    }

    // 정적 팩토리 메서드: "읽고 → 해석해서 → 객체를 만든다"를 한 번에
    public static HttpRequest from(BufferedReader br) throws IOException {
        String firstLine = br.readLine();
        if (firstLine == null) {
            throw new EOFException("Empty request");
        }
        HttpStartLine startLine = HttpStartLine.from(firstLine);   
        HttpHeaders headers = HttpHeaders.from(br);               

        String rawParameters = startLine.getQueryString();         // GET이면 ? 뒤
        if (startLine.getMethod() == HttpMethod.POST) {
            rawParameters = IOUtils.readData(br, headers.getContentLength());  // POST면 본문
        }
        Map<String, String> parameters = HttpRequestUtils.parseQueryParameter(rawParameters);

        return new HttpRequest(startLine, headers, parameters);
    }

    public HttpMethod getMethod() {
        return startLine.getMethod();
    }

    public String getPath() {
        return startLine.getPath();
    }

    public String getVersion() {
        return startLine.getVersion();
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    // isLogined()를 이쪽으로 옮김. Cookie: a=1; logined=true → getCookie("logined") == "true"
    public String getCookie(String name) {
        String cookieHeader = headers.get(HttpHeader.COOKIE);
        if (cookieHeader == null) {
            return null;
        }
        for (String pair : cookieHeader.split(";")) {
            String[] keyValue = pair.trim().split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(name)) {
                return keyValue[1];
            }
        }
        return null;
    }
}
