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
    public void execute(HttpRequest request, HttpResponse response)
            throws IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        User user = userId == null || userId.isBlank()
                ? null
                : repository.findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {

            response.redirect(
                    "/index.html",
                    Map.of("Set-Cookie", "logined=true; Path=/")
            );

        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}