package controller;

import java.io.IOException;

import http.HttpRequest;
import http.HttpResponse;

public class StaticFileController implements Controller {

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        response.forward(request.getPath());
    }
}