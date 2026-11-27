package controller;

import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.io.IOException;

public class LoginController implements Controller {

    private final Repository repository;

    public LoginController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response)
            throws IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        User user = userId == null ? null
                : repository.findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response.addHeader("Set-Cookie", "logined=true; Path=/");
            response.redirect("/index.html");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}