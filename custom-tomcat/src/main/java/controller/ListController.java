package controller;

import java.io.IOException;

import http.HttpRequest;
import http.HttpResponse;

public class ListController implements Controller {

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {

        String cookieHeader = request.getHeader("Cookie");
        boolean isLogined = false;

        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");

            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=", 2);

                if (parts.length == 2
                        && parts[0].trim().equals("logined")
                        && parts[1].trim().equals("true")) {
                    isLogined = true;
                    break;
                }
            }
        }

        if (!isLogined) {
            response.redirect("/user/login.html");
            return;
        }

        response.forward("/user/list.html");
    }
}