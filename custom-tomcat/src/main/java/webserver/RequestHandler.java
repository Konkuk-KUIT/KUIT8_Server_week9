package webserver;

import http.HttpRequest;
import http.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private final RequestMapper mapper;

    public RequestHandler(Socket connection, RequestMapper mapper) {

        this.connection = connection;
        this.mapper=mapper;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            HttpRequest request = HttpRequest.from(br);
            if (request == null) {
                return;
            }
            HttpResponse response = new HttpResponse(out);
            mapper.handle(request, response);



        }catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}

