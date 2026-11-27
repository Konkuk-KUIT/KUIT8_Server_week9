package webserver;

import http.HttpHeader;
import http.HttpMethod;
import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final BufferedReader reader;
    private HttpMethod method;
    private String url;
    private String version;
    private String body = "";
    private Map<String, String> parameter = new HashMap<>();
    private Map<String, String> header;
    private Map<String, String> cookies = new HashMap<>();





    public HttpRequest(BufferedReader reader) throws IOException {
        this.reader = reader;
        parseRequestLine();
        parseHeader();
        parseBody();
        parseCookies(header.get(HttpHeader.COOKIE.getValue()));

    }

    private void parseRequestLine() throws IOException {
        String requestLine = reader.readLine(); //헤더 첫 번째 줄
        if(requestLine == null || requestLine.isEmpty()){
             throw new IOException("빈 요청");
        }
        String[] tokens = requestLine.split(" ");
        if (tokens.length != 3) {
            throw new IOException("잘못된 요청 라인: " + requestLine);
        }
        try{
            this.method = HttpMethod.valueOf(tokens[0]);
        }catch (IllegalArgumentException e){
            throw new IOException("지원하지 않는 메서드: "+tokens[0]);
        }
        this.url = tokens[1];
        this.version = tokens[2];
    }

    private void parseHeader() throws IOException {
        //헤더 읽기
        this.header = new HashMap<>();
        String line = reader.readLine();
        while(line != null && !line.isEmpty()){
            String[] kv = line.split(":", 2);
            header.put(kv[0].trim(), kv[1].trim());
            line = reader.readLine();
        }

    }

    private void parseBody() throws IOException {
        int contentLength;
        try {
            contentLength = Integer.parseInt(header.getOrDefault("Content-Length", "0").trim());
        } catch (NumberFormatException e) {
            throw new IOException("잘못된 Content-Length");
        }
        if (contentLength < 0) {
            throw new IOException("음수 Content-Length");
        }

        //body 부분 추출 및 body부분 파라미터 map으로 변환
        if(contentLength > 0){
            this.body = IOUtils.readData(reader, contentLength);
            parameter = HttpRequestUtils.parseQueryParameter(body);
        }

    }

    private void parseCookies(String cookieHeader) {
       cookies = new HashMap<>();
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return;
        }
        for (String pair : cookieHeader.split(";")) {
            String[] kv = pair.trim().split("=", 2);
            if (kv.length == 2) {
                cookies.put(kv[0].trim(), kv[1].trim());
            }
        }

    }

    public HttpMethod getMethod() { return method; }
    public String getUrl() { return url; }
    public String getVersion() { return version; }
    public Map<String, String> getHeader(){return  header;}
    public Map<String, String> getParameter(){return  parameter;}
    public String getCookie(String key){return cookies.get(key);}
}
