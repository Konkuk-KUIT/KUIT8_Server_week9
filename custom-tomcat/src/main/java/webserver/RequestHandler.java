package webserver;

import http.HttpRequest;
import http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    private final Socket connection;
    private final FrontController frontController;

    public RequestHandler(Socket connection, FrontController frontController) {
        this.connection = connection;
        this.frontController = frontController;
    }

    @Override
    public void run() {
        try (Socket socket = connection;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            HttpRequest request = HttpRequest.from(reader);

            if (request == null) {
                return;
            }

            HttpResponse response = new HttpResponse(socket.getOutputStream());
            frontController.dispatch(request, response);
        } catch (IOException | IllegalArgumentException e) {
            log.log(Level.WARNING, "요청 처리를 종료합니다.", e);
        }
    }
}
