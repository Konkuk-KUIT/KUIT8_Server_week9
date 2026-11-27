package webserver;

import controller.*;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static http.HttpMethod.*;

public class RequestMapper {
    private final Map<RequestKey, Controller> controllers;
    private final Controller staticFile;

    public RequestMapper(Map<RequestKey, Controller> controllers, Controller staticFile) {
        this.controllers = controllers;
        this.staticFile = staticFile;

    }

    public Controller find(HttpRequest request) {
        Controller c = controllers.get(new RequestKey(request.getMethod(), request.getPath()));
        if (c!=null) return c;
        Path file = Path.of("webapp",request.getPath()).normalize();
        if (file.startsWith("webapp") && Files.isRegularFile(file)) return staticFile;
        return(req,res)->res.notFound();
    }

    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        find(request).execute(request,response);
    }
}
