package webserver;

import db.MemoryUserRepository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {

    private final Socket connection;

    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("New Client Connect! Connected IP : "
                + connection.getInetAddress()
                + ", Port : " + connection.getPort());

        try (Socket socket = connection;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            HttpResponse response = new HttpResponse(out);

            try {
                // 요청 분석은 HttpRequest 담당
                HttpRequest request = HttpRequest.from(reader);

                if (request == null) return;

                // 기능 선택은 RequestHandler 담당
                handle(request, response);

            } catch (IllegalArgumentException e) {
                log.log(Level.WARNING, "잘못된 HTTP 요청", e);
                response.error(HttpStatus.BAD_REQUEST);
            }

        } catch (IOException e) {
            log.log(Level.WARNING, "요청 처리 실패", e);
        }
    }

    // URL과 HTTP 메서드에 따라 기능 선택
    private void handle(HttpRequest request, HttpResponse response) throws IOException {

        String path = request.getPath();
        HttpMethod method = request.getMethod();

        // 회원가입
        if (path.equals("/user/signup")) {

            if (method == HttpMethod.GET || method == HttpMethod.POST) {
                signUp(request, response);
            } else {
                response.error(HttpStatus.METHOD_NOT_ALLOWED);
            }
            return;
        }

        // 로그인
        if (path.equals("/user/login")) {
            if (method == HttpMethod.POST) {
                login(request, response);
            } else {
                response.error(HttpStatus.METHOD_NOT_ALLOWED);
            }
            return;
        }

        // 사용자 목록 접근 제어
        if (path.equals("/user/userList") || path.equals("/user/list.html")) {
            if (method != HttpMethod.GET) {
                response.error(HttpStatus.METHOD_NOT_ALLOWED);
            } else if (hasLoginCookie(request.getHeader("Cookie"))) {
                response.forward("/user/list.html");
            } else {
                response.redirect("/user/login.html");
            }
            return;
        }

        // 일반 정적 파일 반환
        if (method == HttpMethod.GET) {
            response.forward(path);
        } else {
            response.error(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    // 회원가입 기능
    private void signUp(HttpRequest request, HttpResponse response) throws IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        if (userId == null || userId.isBlank() || password == null || name == null || email == null) {
            response.error(HttpStatus.BAD_REQUEST);
            return;
        }

        MemoryUserRepository.getInstance().addUser(new User(userId, password, name, email));

        log.info("회원가입 성공: " + userId);

        response.redirect("/index.html");
    }

    // 로그인 기능
    private void login(HttpRequest request, HttpResponse response) throws IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        User user = userId == null || userId.isBlank()
                ? null
                : MemoryUserRepository.getInstance()
                .findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {

            response.redirect(
                    "/index.html",
                    Map.of("Set-Cookie", "logined=true; Path=/")
            );

        } else {
            response.redirect("/user/login_failed.html");
        }
    }

    // 기존 JUnit 테스트와 동일한 쿠키 판별 기능 유지
    static boolean hasLoginCookie(String cookieHeader) {

        if (cookieHeader == null) return false;

        for (String cookie : cookieHeader.split(";")) {

            String[] pair = cookie.trim().split("=", 2);

            if (pair.length == 2
                    && pair[0].trim().equals("logined")
                    && pair[1].trim().equals("true")) {
                return true;
            }
        }

        return false;
    }
}

