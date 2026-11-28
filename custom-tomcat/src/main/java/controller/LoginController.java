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
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> parameters = request.form();
        String userId = parameters.get("userId");
        String password = parameters.get("password");
        User savedUser = userId == null ? null : repository.findUserById(userId);

        if (savedUser != null && password != null && password.equals(savedUser.getPassword())) {
            response.redirect("/index.html", "logined=true; Path=/");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}
