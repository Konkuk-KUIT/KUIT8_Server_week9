package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class HttpResponse {
    private byte[] body;
    private final DataOutputStream dos;

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }

    //serverResource는 단순하게 path에 대한 resource를 serve만 하면 된다.
    public void serveResource(String path) throws IOException {
        File webServer = new File(path);

        if(webServer.exists() && webServer.isFile()){
            body = Files.readAllBytes(webServer.toPath());
        } else {
            notfound();
            return;
        }

        response200Header(dos, body.length, path);
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String path) throws IOException {
        dos.writeBytes("HTTP/1.1 200 OK \r\n");
        if (path.endsWith(".html")){
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
        } else {
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
        }
        dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
        dos.writeBytes("\r\n");
    }

    //redirect Resource 역시 마찬가지이다.
    public void redirect(String redirectUrl) throws IOException {
        body = new byte[0];
        response302Header(dos, redirectUrl);
        responseBody(dos, body);
    }

    public void redirectLogin(String redirectUrl, boolean isLoginSuccess) throws IOException {
        body = new byte[0];
        response302Header(dos, redirectUrl, isLoginSuccess);
        responseBody(dos, body);
    }

    private void response302Header(DataOutputStream dos, String redirectUrl) throws IOException {
        dos.writeBytes("HTTP/1.1 302 OK \r\n");
        dos.writeBytes("Location: " + redirectUrl + "\r\n");
        dos.writeBytes("Content-Length: 0\r\n");
        dos.writeBytes("\r\n");
    }

    private void response302Header(DataOutputStream dos, String redirectUrl, boolean isloginSuccess) throws IOException {
        dos.writeBytes("HTTP/1.1 302 OK \r\n");
        dos.writeBytes("Location: " + redirectUrl + "\r\n");
        dos.writeBytes("Set-Cookie: " + isloginSuccess + "\r\n");
        dos.writeBytes("Content-Length: 0\r\n");
        dos.writeBytes("\r\n");
    }

    private void notfound() throws IOException {
        body = new byte[0];
        response404header(dos);
        responseBody(dos, body);
    }

    private void response404header(DataOutputStream dos) throws IOException {
        dos.writeBytes("HTTP/1.1 404 NotFound \r\n");
        dos.writeBytes("Content-Length: 0\r\n");
        dos.writeBytes("\r\n");
    }

    private void responseBody(DataOutputStream dos, byte[] body) throws IOException {
        dos.write(body, 0, body.length);
        dos.flush();
    }
}
