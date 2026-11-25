package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;
import java.nio.file.Path;

public class ListController implements Controller {
    private boolean isLoggedIn(String cookieHeader) {
        if (cookieHeader == null) {
            return false;
        }

        for (String cookie : cookieHeader.split(";")) {
            String[] pair = cookie.trim().split("=", 2);
            if (pair.length == 2
                    && "logined".equals(pair[0].trim())
                    && "true".equals(pair[1].trim())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        if (isLoggedIn(request.headers().get("Cookie"))) {
            response.forward(Path.of("webapp", "user", "list.html"));
        } else {
            response.redirect("/user/login.html");
        }
    }
}


