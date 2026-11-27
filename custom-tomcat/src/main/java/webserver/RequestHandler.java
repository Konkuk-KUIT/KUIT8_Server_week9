
package webserver;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import db.MemoryUserRepository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
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
                + connection.getInetAddress() + ", Port : "
                + connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            HttpRequest request = HttpRequest.from(br);
            HttpResponse response = new HttpResponse(dos);

            HttpMethod method = request.getMethod();
            String path = request.getPath();

            // 회원가입
            if (path.equals("/user/signup")
                    && (method == HttpMethod.GET || method == HttpMethod.POST)) {

                User user = new User(
                        request.getParameter("userId"),
                        request.getParameter("password"),
                        request.getParameter("name"),
                        request.getParameter("email")
                );

                MemoryUserRepository.getInstance().addUser(user);
                response.redirect("/index.html");
                return;
            }

            // 로그인
            if (path.equals("/user/login") && method == HttpMethod.POST) {
                String userId = request.getParameter("userId");

                User user = userId == null ? null :
                        MemoryUserRepository.getInstance().findUserById(userId);

                if (user != null
                        && user.getPassword().equals(request.getParameter("password"))) {
                    response.redirect("/index.html", "logined=true; Path=/");
                } else {
                    response.redirect("/user/login_failed.html");
                }
                return;
            }

            // 사용자 목록 접근 제어
            if (path.equals("/user/userList") || path.equals("/user/list.html")) {
                if (!request.isLogined()) {
                    response.redirect("/user/login.html");
                    return;
                }

                path = "/user/list.html";
            }

            if (path.equals("/")) {
                path = "/index.html";
            }

            response.forward(path);

        } catch (IOException | IllegalArgumentException e) {
            log.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        }
    }
}
