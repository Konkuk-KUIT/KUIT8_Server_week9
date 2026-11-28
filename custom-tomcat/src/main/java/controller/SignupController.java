package controller;

import db.Repository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.io.IOException;
import java.util.Map;

public class SignupController implements Controller {
    private final Repository repository;

    public SignupController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> parameters = request.method() == HttpMethod.GET
                ? request.query()
                : request.form();
        String userId = parameters.get("userId");

        if (userId == null) {
            throw new IllegalArgumentException("회원가입 요청에 userId가 없습니다.");
        }

        User user = new User(
                userId,
                parameters.get("password"),
                parameters.get("name"),
                parameters.get("email")
        );

        repository.addUser(user);
        response.redirect("/index.html");
    }
}
