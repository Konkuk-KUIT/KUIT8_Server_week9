package controller;

import http.HttpRequest;
import http.HttpResponse;
import java.io.IOException;

public class ListController implements Controller {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        if (!"true".equals(request.getCookie("logined"))) {
            response.redirect("/user/login.html");
            return;
        }

        response.forward("/user/list.html");
    }
}
