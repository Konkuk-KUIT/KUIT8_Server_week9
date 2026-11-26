package controller;

import db.Repository;
import http.request.HttpRequest;
import http.response.HttpResponse;
import model.User;

import java.io.IOException;

//회원가입
public class SignUpController implements Controller {
    private final Repository repository;

    public SignUpController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email"));
        repository.addUser(user);
        response.redirect("/index.html");
    }
}