package webserver.controller;

import db.Repository;
import http.util.HttpRequestUtils;
import model.User;
import webserver.HttpMethod;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LoginController implements Controller {

    Repository userRepository;

    public LoginController(Repository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void execute(HttpRequest req, HttpResponse res) throws IOException {
        Map<String, String> parseRequestBody = HttpRequestUtils.parseQueryParameter(req.getRequestBody());

        String loginUserId = parseRequestBody.get("userId");
        String loginUserPw = parseRequestBody.get("password");

        List<User> userList = userRepository.findAll().stream().toList();

        boolean isLoginSuccess = false;
        for (var usr : userList) {
            System.out.println(loginUserId);
            System.out.println(usr.getUserId());
            System.out.println(loginUserPw);
            System.out.println(usr.getPassword());

            if (usr.getUserId().equals(loginUserId) && usr.getPassword().equals(loginUserPw)) {
                System.out.println("success");
                isLoginSuccess = true;
                break;
            }
        }

        if (isLoginSuccess) {
            res.redirectLogin("/index.html", isLoginSuccess);
        } else {
            res.redirectLogin("/user/login_failed.html", isLoginSuccess);
        }
    }

}
