package webserver;

import controller.Controller;
import http.HttpRequest;
import http.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private final RequestMapper requestMapper;

    public RequestHandler(Socket connection, RequestMapper requestMapper) {
        this.connection = connection;
        this.requestMapper = requestMapper;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : "
                + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            // 요청을 읽는 작업은 HttpRequest가 담당한다.
            HttpRequest request = HttpRequest.from(br);
            HttpResponse response = new HttpResponse(dos);

            // Controller를 고르는 작업은 RequestMapper가 담당한다.
            Controller controller = requestMapper.getController(request);
            controller.service(request, response);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}
