package webserver.controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class StaticFileController implements Controller {
    @Override
    public void execute(HttpRequest req, HttpResponse res) throws IOException {
        res.serveResource(req.getPath());
    }
}
