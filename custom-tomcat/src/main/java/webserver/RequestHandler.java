package webserver;

import controller.*;
import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import http.HttpMethod;
import http.HttpStatus;

public class RequestHandler implements Runnable{
    Socket connection;
    RequestMapper mapper;
    StaticFileController staticFiles;

    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection,
                          RequestMapper mapper,
                          StaticFileController staticFiles) {
        this.connection = connection;
        this.mapper = mapper;
        this.staticFiles = staticFiles;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            HttpResponse response = new HttpResponse(out);

            HttpRequest request = HttpRequest.from(br);

            if (request == null) {
                return;
            }

            Controller controller = mapper.find(request.startLine());

            if (controller != null) {
                controller.execute(request, response);
                return;
            }

            staticFiles.execute(request, response);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String cType) {
        try {
            dos.writeBytes(HttpStatus.OK.statusLine());
            dos.writeBytes("Content-Type: "+cType+";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos,String location, String cookie) {
        try {
            dos.writeBytes(HttpStatus.FOUND.statusLine());
            dos.writeBytes("Location: "+location+"\r\n");
            if (cookie != null) {
                dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            }
            dos.writeBytes("Content-Length: 0 \r\n");
            dos.writeBytes("Connection: close \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}