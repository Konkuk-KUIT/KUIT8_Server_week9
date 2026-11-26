package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.IOException;

//메인 화면을 보여줌.
public class HomeController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        response.forward("/index.html");
    }
}