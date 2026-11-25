package webserver.controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class HomeController implements Controller {
    @Override
    public void execute(HttpRequest req, HttpResponse res) throws IOException {
        res.redirect("/index.html");
    }
}
