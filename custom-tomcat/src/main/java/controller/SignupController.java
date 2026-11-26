package controller;

import db.Repository;
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
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> parameters = request.getParameters();
        String userId = parameters.get("userId");
        String password = parameters.get("password");
        String name = parameters.get("name");
        String email = parameters.get("email");
        if (userId == null || password == null || name == null || email == null) {
            throw new IllegalArgumentException("회원가입 항목이 누락되었습니다.");
        }
        repository.addUser(new User(userId, password, name, email));
        response.redirect("/index.html");
    }
}
