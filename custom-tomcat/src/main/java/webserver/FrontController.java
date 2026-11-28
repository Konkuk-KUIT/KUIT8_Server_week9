package webserver;

import controller.Controller;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class FrontController {
    private final RequestMapper requestMapper;

    public FrontController(RequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

    public void dispatch(HttpRequest request, HttpResponse response) throws IOException {
        Controller controller = requestMapper.find(request.method(), request.path());

        if (controller != null) {
            controller.handle(request, response);
        } else if (request.method() == HttpMethod.GET) {
            response.forward(request.path());
        } else {
            response.notFound();
        }
    }
}
