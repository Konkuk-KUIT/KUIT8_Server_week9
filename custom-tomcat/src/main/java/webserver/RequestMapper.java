package webserver;

import controller.Controller;
import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class RequestMapper {
    private final Map<RequestKey, Controller> controllers;
    private final Controller staticFileController;

    public RequestMapper(Map<RequestKey, Controller> controllers, Controller staticFileController) {
        this.controllers = Map.copyOf(controllers);
        this.staticFileController = staticFileController;
    }

    // 일꾼 찾기
    public Controller findController(HttpRequest request) {
        RequestKey key = new RequestKey(request.getMethod(), request.getPath());
        return controllers.getOrDefault(key, staticFileController);
    }

    // 찾은 일꾼에게 일 시키기
    public void proceed(HttpRequest request, HttpResponse response) throws IOException {
        findController(request).execute(request, response);
    }
}