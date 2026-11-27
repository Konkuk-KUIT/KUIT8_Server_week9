package webserver;

import controller.Controller;
import controller.StaticFileController;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {

    private static final Logger log =
            Logger.getLogger(RequestHandler.class.getName());

    private final Socket connection;
    private final RequestMapper requestMapper;
    private final StaticFileController staticFileController;

    public RequestHandler(Socket connection,
                          RequestMapper requestMapper,
                          StaticFileController staticFileController) {
        this.connection = connection;
        this.requestMapper = requestMapper;
        this.staticFileController = staticFileController;
    }

    @Override
    public void run() {
        log.info("Connected IP: " + connection.getInetAddress()
                + ", Port: " + connection.getPort());

        try (Socket socket = connection;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(),
                             StandardCharsets.UTF_8));
             DataOutputStream out =
                     new DataOutputStream(socket.getOutputStream())) {

            HttpResponse response = new HttpResponse(out);

            try {
                HttpRequest request = HttpRequest.from(reader);
                route(request, response);
            } catch (IllegalArgumentException e) {
                response.sendText(HttpStatus.BAD_REQUEST, "Bad Request");
            }

        } catch (IOException e) {
            log.log(Level.SEVERE, "오류가 발생했습니다.", e);
        }
    }

    private void route(HttpRequest request, HttpResponse response)
            throws IOException {

        Controller controller = requestMapper.findController(request);

        if (controller != null) {
            controller.execute(request, response);
            return;
        }

        if (request.getMethod() == HttpMethod.GET) {
            staticFileController.execute(request, response);
            return;
        }

        response.sendNotFound();
    }
}