package webserver;

import controller.Controller;
import controller.StaticFileController;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import java.io.IOException;
import java.util.Map;

public class RequestMapper {
    private final Map<RequestKey, Controller> controllers;
    private final StaticFileController staticFileController;
    private final Controller notFoundController;

    public RequestMapper(Map<RequestKey, Controller> controllers,
                         StaticFileController staticFileController,
                         Controller notFoundController) {
        this.controllers = Map.copyOf(controllers);
        this.staticFileController = staticFileController;
        this.notFoundController = notFoundController;
    }

    public Controller findController(HttpRequest request) {
        Controller controller = controllers.get(new RequestKey(request.getMethod(), request.getPath()));
        if (controller != null) {
            return controller;
        }
        if (request.getMethod() == HttpMethod.GET && staticFileController.hasFile(request.getPath())) {
            return staticFileController;
        }
        return notFoundController;
    }

    public void dispatch(HttpRequest request, HttpResponse response) throws IOException {
        findController(request).execute(request, response);
    }
}
