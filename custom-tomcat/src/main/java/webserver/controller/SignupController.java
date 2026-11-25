package webserver.controller;

import db.Repository;
import http.util.HttpRequestUtils;
import model.User;
import webserver.HttpMethod;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class SignupController implements Controller {
    Repository userRepository;

    public SignupController(Repository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void execute(HttpRequest req, HttpResponse res) throws IOException {
        if(req.getHttpMethod() == HttpMethod.GET){
            createUser(res, req.getQueryParameter());

        } else if(req.getHttpMethod() == HttpMethod.POST){
            Map<String, String> parseRequestBody = HttpRequestUtils.parseQueryParameter(req.getRequestBody());
            createUser(res, parseRequestBody);

        }
    }

    private void createUser(HttpResponse res, Map<String, String> parseQueryParameter) throws IOException {
        String userId = parseQueryParameter.get("userId");
        String password = parseQueryParameter.get("password");
        String name = parseQueryParameter.get("name");
        String email = parseQueryParameter.get("email");

        User user = new User(userId, password, name, email);

        userRepository.addUser(user);

        res.redirect("/index.html");
    }
}
