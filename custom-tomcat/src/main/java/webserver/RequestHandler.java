package webserver;

import controller.ListController;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {

    private final Socket connection;
    private final RequestMapper requestMapper;

    private static final Logger log =
            Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection, RequestMapper requestMapper) {
        this.connection = connection;
        this.requestMapper = requestMapper;
    }

    @Override
    public void run() {

        log.info("New Client Connect! Connected IP : "
                + connection.getInetAddress()
                + ", Port : " + connection.getPort());

        try (Socket socket = connection;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8)
            );

            HttpResponse response = new HttpResponse(out);

            try {

                HttpRequest request = HttpRequest.from(reader);

                if (request == null) return;

                // 요청에 해당하는 컨트롤러를 찾아 실행
                requestMapper.dispatch(request, response);

            } catch (IllegalArgumentException e) {

                log.log(Level.WARNING, "잘못된 HTTP 요청", e);
                response.error(HttpStatus.BAD_REQUEST);
            }

        } catch (IOException e) {
            log.log(Level.WARNING, "요청 처리 실패", e);
        }
    }

    // 기존 8주차 쿠키 검사 테스트와의 호환성 유지
    static boolean hasLoginCookie(String cookieHeader) {
        return ListController.hasLoginCookie(cookieHeader);
    }
}

