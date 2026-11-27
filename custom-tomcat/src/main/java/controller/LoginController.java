package controller;

import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import java.io.IOException;

public class LoginController implements Controller {
    private final Repository userRepository;

    public LoginController(Repository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        User user = userRepository.findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response.setCookie("logined=true; Path=/");
            response.redirect("/index.html");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}
