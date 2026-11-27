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
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        // 로그인
        String userId = request.getParameter("userId");
        User user = userId == null ? null : repository.findUserById(userId);
        if (user != null && user.getPassword().equals(request.getParameter("password"))) {
            response.redirect("/index.html", "logined=true; Path=/");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}
