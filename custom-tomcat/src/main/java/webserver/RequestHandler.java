package webserver;

import db.MemoryUserRepository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {

    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    private final Socket connection;

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("Connected IP: " + connection.getInetAddress() + ", Port: " + connection.getPort());

        try (Socket socket = connection;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
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

    private void route(HttpRequest request, HttpResponse response) throws IOException {

        String path = request.getPath();
        HttpMethod method = request.getMethod();

        if (method == HttpMethod.POST && path.equals("/user/signup")) {
            handleSignup(request, response);
            return;
        }

        if (method == HttpMethod.POST && path.equals("/user/login")) {
            handleLogin(request, response);
            return;
        }

        if (method == HttpMethod.GET && (path.equals("/user/userList") || path.equals("/user/list.html"))) {

            if (request.isLoggedIn()) {
                response.forward("/user/list.html");
            } else {
                response.redirect("/user/login.html");
            }

            return;
        }

        if (method == HttpMethod.GET) {
            response.forward(path);
            return;
        }

        response.sendText(HttpStatus.NOT_FOUND, "Not Found");
    }

    private void handleSignup(HttpRequest request, HttpResponse response) throws IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        if (isBlank(userId) || isBlank(password) || isBlank(name) || isBlank(email)) {
            response.sendText(HttpStatus.BAD_REQUEST, "빈칸을 확인해주세요");
            return;
        }

        User user = new User(userId, password, name, email);
        MemoryUserRepository.getInstance().addUser(user);

        response.redirect("/index.html");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void handleLogin(HttpRequest request, HttpResponse response) throws IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        User user = userId == null ? null : MemoryUserRepository.getInstance().findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response.addHeader("Set-Cookie", "logined=true; Path=/");
            response.redirect("/index.html");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}
