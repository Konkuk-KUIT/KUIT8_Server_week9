package webserver;

import db.MemoryUserRepository;
import http.HttpMethod;
import  webserver.controller.*;
import java.util.HashMap;
import java.util.Map;

public class RequestMapper {

    private final Map<RequestKey, Controller> controllers;
    private final Controller defaultController;
    public RequestMapper(Map<RequestKey, Controller> controllers, Controller defaultController){
        this.controllers = controllers;
        this.defaultController = defaultController;
    }

    public Controller getController(HttpRequest request) {
        RequestKey key = new RequestKey(request.getMethod(), request.getUrl());
        return controllers.getOrDefault(key, defaultController);
    }

}