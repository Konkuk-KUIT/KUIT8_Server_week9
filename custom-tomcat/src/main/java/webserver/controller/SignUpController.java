package webserver.controller;

import db.MemoryUserRepository;
import http.HttpHeader;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class SignUpController implements Controller{

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> parameter = request.getParameter();
        User user = new User(parameter.get("userId"), parameter.get("password"), parameter.get("name"), parameter.get("email"));
        MemoryUserRepository.getInstance().addUser(user);
        response.addHeader(HttpHeader.SET_COOKIE, "logined=true; Path=/");
        response.redirect("/index.html");
    }
}
