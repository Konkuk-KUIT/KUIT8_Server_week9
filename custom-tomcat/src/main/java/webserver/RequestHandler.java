package webserver;

import db.MemoryUserRepository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private final Socket connection;

    private static final Logger log =
            Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try (Socket socket = connection;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream(),
                             StandardCharsets.UTF_8
                     )
             )) {

            HttpRequest request = HttpRequest.from(reader);

            if (request == null) {
                return;
            }

            HttpResponse response =
                    new HttpResponse(socket.getOutputStream());

            HttpMethod method = request.method();
            String path = request.path();

            if (path.equals("/user/signup")
                    && (method == HttpMethod.GET
                    || method == HttpMethod.POST)) {

                Map<String, String> parameters =
                        method == HttpMethod.GET
                                ? request.query()
                                : request.form();

                signup(parameters, response);
                return;
            }

            if (method == HttpMethod.POST
                    && path.equals("/user/login")) {
                login(request.form(), response);
                return;
            }

            if (method != HttpMethod.GET) {
                response.notFound();
                return;
            }

            if (path.equals("/user/userList")
                    || path.equals("/user/list.html")) {

                if (!request.isLoggedIn()) {
                    response.redirect("/user/login.html");
                    return;
                }

                response.forward("/user/list.html");
                return;
            }

            if (path.equals("/")) {
                path = "/index.html";
            }

            response.forward(path);

        } catch (IOException | IllegalArgumentException e) {
            log.log(Level.WARNING, "요청 처리를 종료합니다.", e);
        }
    }

    private void signup(
            Map<String, String> parameters,
            HttpResponse response
    ) throws IOException {

        String userId = parameters.get("userId");

        if (userId == null) {
            throw new IllegalArgumentException(
                    "회원가입 요청에 userId가 없습니다."
            );
        }

        User user = new User(
                userId,
                parameters.get("password"),
                parameters.get("name"),
                parameters.get("email")
        );

        MemoryUserRepository.getInstance().addUser(user);
        response.redirect("/index.html");
    }

    private void login(
            Map<String, String> parameters,
            HttpResponse response
    ) throws IOException {

        String userId = parameters.get("userId");
        String password = parameters.get("password");

        User savedUser = null;

        if (userId != null) {
            savedUser = MemoryUserRepository.getInstance()
                    .findUserById(userId);
        }

        boolean loginSuccess =
                savedUser != null
                        && password != null
                        && password.equals(savedUser.getPassword());

        if (loginSuccess) {
            response.redirect(
                    "/index.html",
                    "logined=true; Path=/"
            );
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}