
package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import db.MemoryUserRepository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;

public class RequestHandler implements Runnable {

    Socket connection;

    private static final Logger log =
            Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
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
            String url = request.getPath();
            int contentLength = request.getContentLength();
            String cookieHeader = request.getHeader("Cookie");

            // 4. GET 및 POST 요청 처리
            if (method != HttpMethod.GET && method != HttpMethod.POST) {
                response.error(HttpStatus.METHOD_NOT_ALLOWED);
                return;
            }

            // GET 및 POST 회원가입 처리
            if (url.equals("/user/signup")) {

                if (method == HttpMethod.POST && contentLength < 0) {
                    response.error(HttpStatus.BAD_REQUEST);
                    return;
                }

                Map<String, String> parameters = request.getParameters();

                String userId = parameters.get("userId");
                String password = parameters.get("password");
                String name = parameters.get("name");
                String email = parameters.get("email");

                if (userId == null || userId.isBlank()
                        || password == null || password.isBlank()
                        || name == null || name.isBlank()
                        || email == null || email.isBlank()) {
                    response.error(HttpStatus.BAD_REQUEST);
                    return;
                }

                User user = new User(userId, password, name, email);

                MemoryUserRepository.getInstance().addUser(user);

                System.out.println("Sign up success: " + userId);

                response.redirect("/index.html");
                return;
            }

            // POST 로그인 처리
            if (url.equals("/user/login") && method == HttpMethod.POST) {

                if (contentLength < 0) {
                    response.error(HttpStatus.BAD_REQUEST);
                    return;
                }

                Map<String, String> parameters = request.getParameters();

                String userId = parameters.get("userId");
                String password = parameters.get("password");

                User user = null;

                if (userId != null) {
                    user = MemoryUserRepository.getInstance()
                            .findUserById(userId);
                }

                if (user != null && user.getPassword().equals(password)) {
                    System.out.println("Login success: " + userId);

                    response.redirect(
                            "/index.html",
                            "logined=true; Path=/"
                    );
                } else {
                    System.out.println("Login failed: " + userId);

                    response.redirect("/user/login_failed.html");
                }

                return;
            }

            if (method != HttpMethod.GET) {
                response.error(HttpStatus.METHOD_NOT_ALLOWED);
                return;
            }

            // 사용자 목록 접근 제어
            if (url.equals("/user/userList") || url.equals("/user/list.html")) {

                boolean isLogined = false;

                if (cookieHeader != null) {
                    String[] cookies = cookieHeader.split(";");

                    for (String cookie : cookies) {
                        String[] parts = cookie.trim().split("=", 2);

                        if (parts.length == 2
                                && parts[0].trim().equals("logined")
                                && parts[1].trim().equals("true")) {
                            isLogined = true;
                            break;
                        }
                    }
                }

                if (!isLogined) {
                    response.redirect("/user/login.html");
                    return;
                }

                url = "/user/list.html";
            }

            // 6. 기본 경로 처리
            if (url.equals("/")) {
                url = "/index.html";
            }

            // 정적 파일 응답
            response.forward(url);

        } catch (IOException e) {
            log.log(Level.SEVERE, "Request handling failed", e);
        } catch (IllegalArgumentException e) {
            log.log(Level.WARNING, "Invalid request: " + e.getMessage());
        }
    }
}
