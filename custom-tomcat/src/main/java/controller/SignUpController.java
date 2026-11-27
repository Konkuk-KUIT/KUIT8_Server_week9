package controller;

import java.io.IOException;
import java.util.Map;

import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;

public class SignUpController implements Controller {

    private final Repository repository;

    public SignUpController(Repository repository) {
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
        String name = parameters.get("name");
        String email = parameters.get("email");

        if (userId == null || userId.isBlank()
                || password == null || password.isBlank()
                || name == null || name.isBlank()
                || email == null || email.isBlank()) {
            response.error(HttpStatus.BAD_REQUEST);
            return;
        }

        User user = new User(userId, password, name, email);

        repository.addUser(user);

        System.out.println("Sign up success: " + userId);

        response.redirect("/index.html");
    }
}