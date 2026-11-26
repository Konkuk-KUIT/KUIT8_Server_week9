package webserver;

import db.MemoryUserRepository;
import http.constant.HttpHeader;
import http.constant.HttpMethod;
import http.request.HttpRequest;
import http.response.HttpResponse;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private final MemoryUserRepository repository = MemoryUserRepository.getInstance();

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            HttpRequest request = HttpRequest.from(br);   
            HttpResponse response = new HttpResponse(out);

            String path = request.getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            // ④ 할 일 고르기만 남음
            if (path.equals("/user/signup")) {
                signUp(request, response);
                return;
            }
            if (request.getMethod() == HttpMethod.POST && path.equals("/user/login")) {
                login(request, response);
                return;
            }
            if (path.equals("/user/userList") || path.equals("/user/list.html")) {
                userList(request, response);
                return;
            }
            response.forward(path);                           // 나머지는 파일

        } catch (IOException | RuntimeException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void signUp(HttpRequest request, HttpResponse response) throws IOException {
        User user = new User(request.getParameter("userId"), request.getParameter("password"),
                request.getParameter("name"), request.getParameter("email"));
        repository.addUser(user);
        response.redirect("/index.html");
    }

    private void login(HttpRequest request, HttpResponse response) throws IOException {
        String userId = request.getParameter("userId");
        User user = (userId == null) ? null : repository.findUserById(userId);

        if (user != null && user.getPassword().equals(request.getParameter("password"))) {
            response.addHeader(HttpHeader.SET_COOKIE, "logined=true; Path=/");
            response.redirect("/index.html");
            return;
        }
        response.redirect("/user/login_failed.html");
    }

    private void userList(HttpRequest request, HttpResponse response) throws IOException {
        if (!"true".equals(request.getCookie("logined"))) {
            response.redirect("/user/login.html");
            return;
        }
        response.forward("/user/list.html");
    }
}