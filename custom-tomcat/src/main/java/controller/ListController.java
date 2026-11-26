package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class ListController implements Controller {

    @Override
    public void execute(HttpRequest request, HttpResponse response)
            throws IOException {

        if (hasLoginCookie(request.getHeader("Cookie"))) {
            response.forward("/user/list.html");
        } else {
            response.redirect("/user/login.html");
        }
    }

    public static boolean hasLoginCookie(String cookieHeader) {

        if (cookieHeader == null) return false;

        for (String cookie : cookieHeader.split(";")) {

            String[] pair = cookie.trim().split("=", 2);

            if (pair.length == 2
                    && pair[0].trim().equals("logined")
                    && pair[1].trim().equals("true")) {
                return true;
            }
        }

        return false;
    }
}
