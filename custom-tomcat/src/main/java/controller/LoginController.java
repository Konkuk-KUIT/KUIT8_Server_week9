package controller;

import db.MemoryUserRepository;
import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.io.IOException;

public class LoginController implements Controller{

    private final Repository repository;

    public LoginController(Repository repository) {
        this.repository=repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        User user = repository.findUserById(request.getParameter("userId"));
        if (user != null && user.getPassword().equals(request.getParameter("password"))) {
            response.redirect("/index.html", "logined=true; Path=/");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}
