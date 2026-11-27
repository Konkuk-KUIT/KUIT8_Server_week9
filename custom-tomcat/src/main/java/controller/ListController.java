package controller;

import http.HttpRequest;
import http.HttpResponse;
import java.io.IOException;

public class ListController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        // 사용자 목록 접근 제어
        if (!request.isLogined()) {
            response.redirect("/user/login.html");
            return;
        }
        response.forward("/user/list.html");
    }
}
