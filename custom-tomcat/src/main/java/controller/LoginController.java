package controller;

import java.io.IOException;
import java.util.Map;

import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;

public class LoginController implements Controller {

    private final Repository repository;

    public LoginController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {

        if (request.getContentLength() < 0) {
            response.error(HttpStatus.BAD_REQUEST);
            return;
        }

        Map<String, String> parameters = request.getParameters();

        String userId = parameters.get("userId");
        String password = parameters.get("password");

        User user = null;

        if (userId != null) {
            user = repository.findUserById(userId);
        }

        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Login success: " + userId);

            response.redirect(
                    "/index.html",
                    "logined=true; Path=/"
            );
        } else {
            System.out.println("Login failed: " + userId);

            response.redirect("/user/login_failed.html");
        }
    }
}