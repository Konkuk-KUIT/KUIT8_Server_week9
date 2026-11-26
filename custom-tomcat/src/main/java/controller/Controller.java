package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.IOException;

//프론트 컨트롤러
public interface Controller {
    void execute(HttpRequest request, HttpResponse response) throws IOException;
}