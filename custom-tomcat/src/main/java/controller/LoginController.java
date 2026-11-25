package controller;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class LoginController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        if (request.body().isEmpty()) {
            return;
        }

        Map<String, String> queries = request.formParams();
        String userId = queries.get("userId");
        String password = queries.get("password");
        User user = MemoryUserRepository.getInstance().findUserById(userId);
        if (user == null || !user.getPassword().equals(password)) {
            response.redirect("/user/login_failed.html");
        }
        else{
            response.redirect("/index.html", "logined=true; Path=/");
        }
    }
}
