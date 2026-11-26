package webserver;

import controller.Controller;
import http.HttpMethod;
import http.HttpStartLine;
import http.RequestKey;

import java.util.HashMap;
import java.util.Map;

public class RequestMapper {
    private final Map<RequestKey, Controller> controllers = new HashMap<RequestKey, Controller>();

    public void register(HttpMethod method, String path, Controller controller) {
        controllers.put(new RequestKey(method, path), controller);
    }

    public Controller find(HttpStartLine startLine) {
        return controllers.get(new RequestKey(startLine.method(), startLine.path()));
    }
}
