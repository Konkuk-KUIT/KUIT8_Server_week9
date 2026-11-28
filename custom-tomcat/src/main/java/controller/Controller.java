package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

@FunctionalInterface
public interface Controller {
    void handle(HttpRequest request, HttpResponse response) throws IOException;
}
