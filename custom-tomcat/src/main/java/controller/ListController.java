package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class ListController implements Controller {

    @Override
    public void execute(HttpRequest request, HttpResponse response)
            throws IOException {

        if (request.isLoggedIn()) {
            response.forward("/user/list.html");
        } else {
            response.redirect("/user/login.html");
        }
    }
}