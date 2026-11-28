package webserver;

import controller.Controller;
import http.HttpMethod;

import java.util.Map;

public class RequestMapper {
    private final Map<Route, Controller> controllers;

    public RequestMapper(Map<Route, Controller> controllers) {
        this.controllers = Map.copyOf(controllers);
    }

    public Controller find(HttpMethod method, String path) {
        return controllers.get(new Route(method, path));
    }

    public record Route(HttpMethod method, String path) {
    }
}
