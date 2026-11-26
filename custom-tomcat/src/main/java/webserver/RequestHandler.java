package webserver;

import http.HttpRequest;
import http.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private final Socket connection;
    private final RequestMapper requestMapper;

    public RequestHandler(Socket connection, RequestMapper requestMapper) {
        this.connection = connection;
        this.requestMapper = requestMapper;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress()
                + ", Port : " + connection.getPort());
        try (Socket socket = connection;
             InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.from(reader);
            if (request == null) {
                return;
            }
            log.info("Request: " + request.getMethod() + " " + request.getPath());
            requestMapper.dispatch(request, new HttpResponse(out));
        } catch (IOException | IllegalArgumentException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}
