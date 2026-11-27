package webserver;

import db.MemoryUserRepository;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
        log.log(Level.INFO, "New Client Connect! Connected IP : "
                + connection.getInetAddress() + ", Port : " + connection.getPort());

        try (Socket socket = connection;
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            HttpRequest request = HttpRequest.from(br);
            if (request == null) {
                return;
            }

            HttpResponse response = new HttpResponse(socket.getOutputStream());
            HttpMethod method = request.method();
            String path = request.path();

            if (method == HttpMethod.POST && path.equals("/user/signup")) {
                signup(request.form(), response);
                return;
            }

            if (method == HttpMethod.POST && path.equals("/user/login")) {
                login(request.form(), response);
                return;
            }

            if (method != HttpMethod.GET) {
                return;
            }

            if ((path.equals("/user/userList") || path.equals("/user/list.html"))
                    && !isLoggedIn(request.header("Cookie"))) {
                response.redirect("/user/login.html");
                return;
            }

            Path file = switch (path) {
                case "/", "/index.html" -> Path.of("webapp/index.html");
                case "/user/form.html" -> Path.of("webapp/user/form.html");
                case "/user/login.html" -> Path.of("webapp/user/login.html");
                case "/user/login_failed.html" -> Path.of("webapp/user/login_failed.html");
                case "/user/userList", "/user/list.html" -> Path.of("webapp/user/list.html");
                case "/css/styles.css" -> Path.of("webapp/css/styles.css");
                default -> null;
            };

            if (file != null) {
                response.forward(file);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void signup(Map<String, String> params, HttpResponse response) throws IOException {
        if (!params.keySet().containsAll(List.of("userId", "password", "name", "email"))) {
            return;
        }

        User user = new User(
                params.get("userId"),
                params.get("password"),
                params.get("name"),
                params.get("email")
        );
        MemoryUserRepository.getInstance().addUser(user);
        response.redirect("/index.html");
    }

    private void login(Map<String, String> params, HttpResponse response) throws IOException {
        String userId = params.get("userId");
        String password = params.get("password");
        User user = userId == null ? null : MemoryUserRepository.getInstance().findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response.redirect("/index.html", "logined=true; Path=/");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }

    private boolean isLoggedIn(String cookieHeader) {
        if (cookieHeader == null) {
            return false;
        }

        for (String cookie : cookieHeader.split(";")) {
            if (cookie.trim().equals("logined=true")) {
                return true;
            }
        }

        return false;
    }
}