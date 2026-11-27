package webserver.controller;

import db.MemoryUserRepository;
import db.Repository;
import http.HttpHeader;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class SignUpController implements Controller{
    private final Repository repository;
    public SignUpController(Repository repository){
        this.repository = repository;
    }
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> parameter = request.getParameter();
        User user = new User(parameter.get("userId"), parameter.get("password"), parameter.get("name"), parameter.get("email"));
        repository.addUser(user);
        response.redirect("/index.html");
    }
}
