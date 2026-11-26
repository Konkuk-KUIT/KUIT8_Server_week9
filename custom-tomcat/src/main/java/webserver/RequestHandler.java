package webserver;

import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    
    private final Socket connection;
    private final RequestMapper requestMapper;

    public RequestHandler(Socket connection, RequestMapper requestMapper) {
        this.connection = connection;
        this.requestMapper = requestMapper;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            HttpRequest request = HttpRequest.from(br);   
            HttpResponse response = new HttpResponse(out);

             requestMapper.proceed(request, response);
        } catch (IOException | RuntimeException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}