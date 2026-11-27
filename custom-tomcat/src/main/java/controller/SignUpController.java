package controller;

import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;

import java.io.IOException;

public class SignUpController implements Controller {

    private final Repository repository;

    public SignUpController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response)
            throws IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        if (isBlank(userId) || isBlank(password)
                || isBlank(name) || isBlank(email)) {
            response.sendText(HttpStatus.BAD_REQUEST, "빈칸을 확인해주세요");
            return;
        }

        User user = new User(userId, password, name, email);
        repository.addUser(user);

        response.redirect("/index.html");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}