package controller;

import db.MemoryUserRepository;
import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.io.IOException;

public class SignUpController implements Controller{

    private final Repository repository;

    public SignUpController(Repository repository) {
        this.repository=repository;
    }
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        repository.addUser(new User(
                        request.getParameter("userId"),
                        request.getParameter("password"),
                        request.getParameter("name"),
                        request.getParameter("email")));
        response.redirect("/index.html");
    }
}
