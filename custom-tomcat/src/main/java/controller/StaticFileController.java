package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.IOException;

//css, 그림 등 파일을 보내줌
public class StaticFileController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        response.forward(request.getPath());
    }
}