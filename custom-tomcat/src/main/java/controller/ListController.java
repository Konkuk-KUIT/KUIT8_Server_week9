package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;
import java.util.Arrays;

public class ListController implements Controller{
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        boolean loggedIn = Arrays.stream(request.getHeader("Cookie").split(";"))
                .anyMatch(c -> c.trim().equals("logined=true"));
        if (!loggedIn) {
            response.redirect("/user/login.html");
            return;
        }
        response.forward("/user/list.html");
    }
}
