package webserver;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            HttpRequest request = HttpRequest.from(br);
            if (request == null) {
                return;
            }
            HttpResponse response = new HttpResponse(out);

            String path = request.getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            if (path.equals("/user/signup")) {
                User user = new User(request.getParameter("userId"), request.getParameter("password"),
                        request.getParameter("name"), request.getParameter("email"));
                MemoryUserRepository.getInstance().addUser(user);
                response.redirect("/index.html");
                return;
            }

            if (path.equals("/user/login")) {
                User user = MemoryUserRepository.getInstance().findUserById(request.getParameter("userId"));
                if (user != null && user.getPassword().equals(request.getParameter("password"))) {
                    response.redirect("/index.html", "logined=true; Path=/");
                } else {
                    response.redirect("/user/login_failed.html");
                }
                return;
            }

            if (path.equals("/user/userList") || path.equals("/user/list.html")) {
                boolean loggedIn = false;
                for (String c : request.getHeader("Cookie").split(";")) {
                    if (c.trim().equals("logined=true")) {
                        loggedIn = true;
                    }
                }
                if (!loggedIn) {
                    response.redirect("/user/login.html");
                    return;
                }
                path = "/user/list.html";
            }

            response.forward(path);

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}


