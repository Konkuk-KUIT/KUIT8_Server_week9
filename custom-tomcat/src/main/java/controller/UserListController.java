package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class UserListController implements Controller {
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        if (!request.isLoggedIn()) {
            response.redirect("/user/login.html");
            return;
        }

        response.forward("/user/list.html");
    }
}
