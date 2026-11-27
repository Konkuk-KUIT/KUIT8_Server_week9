
package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;

public class RequestHandler implements Runnable {

    Socket connection;
    private final RequestMapper requestMapper;

    private static final Logger log =
            Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection, RequestMapper requestMapper) {
        this.connection = connection;
        this.requestMapper = requestMapper;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : "
                + connection.getInetAddress()
                + ", Port : " + connection.getPort());

        try (Socket socket = connection;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8)
            );

            HttpResponse response = new HttpResponse(out);

            // HTTP 요청 분석
            HttpRequest request;

            try {
                request = HttpRequest.from(br);
            } catch (UnsupportedOperationException e) {
                response.error(HttpStatus.METHOD_NOT_ALLOWED);
                return;
            } catch (IllegalArgumentException e) {
                response.error(HttpStatus.BAD_REQUEST);
                return;
            }

            if (request == null) {
                return;
            }

            HttpMethod method = request.getMethod();

            // 4. GET 및 POST 요청 처리
            if (method != HttpMethod.GET && method != HttpMethod.POST) {
                response.error(HttpStatus.METHOD_NOT_ALLOWED);
                return;
            }

            // RequestMapper에 요청 처리 위임
            requestMapper.dispatch(request, response);

        } catch (IOException e) {
            log.log(Level.SEVERE, "Request handling failed", e);
        } catch (IllegalArgumentException e) {
            log.log(Level.WARNING, "Invalid request: " + e.getMessage());
        }
    }
}
