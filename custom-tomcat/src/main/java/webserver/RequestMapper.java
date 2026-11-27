package webserver;

import controller.Controller;
import http.HttpMethod;
import http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class RequestMapper {

    private final Map<RequestKey, Controller> mappings = new HashMap<>();

    public void register(HttpMethod method, String path, Controller controller) {
        mappings.put(new RequestKey(method, path), controller);
    }

    public Controller findController(HttpRequest request) {
        return mappings.get(new RequestKey(request.getMethod(), request.getPath()));
    }
}