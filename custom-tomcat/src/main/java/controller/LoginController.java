package controller;

import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.io.IOException;
import java.util.Map;

public class LoginController implements Controller {
    private final Repository repository;

    public LoginController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> parameters = request.getParameters();
        String userId = parameters.get("userId");
        String password = parameters.get("password");
        if (userId == null || password == null) {
            response.redirect("/user/login_failed.html");
            return;
        }
        User user = repository.findUserById(userId);
        if (user != null && password.equals(user.getPassword())) {
            response.redirect("/index.html", "logined=true; Path=/");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}
