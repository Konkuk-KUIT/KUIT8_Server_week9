package controller;

import db.Repository;
import http.constant.HttpHeader;
import http.request.HttpRequest;
import http.response.HttpResponse;
import model.User;

import java.io.IOException;

//로그인
public class LoginController implements Controller {
    private final Repository repository;

    public LoginController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        String userId = request.getParameter("userId");
        User user = (userId == null) ? null : repository.findUserById(userId);

        if (user != null && user.getPassword().equals(request.getParameter("password"))) {
            response.addHeader(HttpHeader.SET_COOKIE, "logined=true; Path=/");
            response.redirect("/index.html");
            return;
        }
        response.redirect("/user/login_failed.html");
    }
}