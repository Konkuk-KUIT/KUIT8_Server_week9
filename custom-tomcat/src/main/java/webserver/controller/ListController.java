package webserver.controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class ListController implements Controller {
    @Override
    public void execute(HttpRequest req, HttpResponse res) throws IOException {
        if (req.isLogin()) {
            res.serveResource("/user/list.html");
        } else {
            res.redirect("/user/login.html");
        }
    }
}
