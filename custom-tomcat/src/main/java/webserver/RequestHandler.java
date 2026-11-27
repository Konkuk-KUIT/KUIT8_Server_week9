package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private final Socket connection;
    private final RequestMapper mapper;

    RequestHandler(Socket connection, RequestMapper mapper) {
        this.connection = connection;
        this.mapper = mapper;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : "
                + connection.getInetAddress() + ", Port : " + connection.getPort());

        try (Socket socket = connection;
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            HttpRequest request = HttpRequest.from(br);
            if (request == null) {
                return;
            }

            HttpResponse response = new HttpResponse(socket.getOutputStream());
            mapper.dispatch(request, response);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}
