package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.IOException;

//회원 목록
public class ListController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        if (!"true".equals(request.getCookie("logined"))) {
            response.redirect("/user/login.html");
            return;
        }
        response.forward("/user/list.html");
    }
}