package webserver;

import controller.Controller;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class RequestMapper {
    private final Map<Route, Controller> controllers;

    public RequestMapper(Map<Route, Controller> controllers) {
        this.controllers = Map.copyOf(controllers);
    }

    public Controller getController(HttpMethod method, String path) {
        if (!path.startsWith("/")) {
            return null;
        }
        Path relativePath = Path.of(path.substring(1)).normalize();
        if (relativePath.getRoot() != null || relativePath.startsWith("..")) {
            return null;
        }
        // 정적 파일과 같은 기준으로 정규화하여 목록 접근 검사를 우회하지 못하게 합니다.
        String normalizedPath = "/" + relativePath.toString().replace('\\', '/');
        return controllers.get(new Route(method, normalizedPath));
    }

    public void dispatch(HttpRequest request, HttpResponse response) throws IOException {
        Controller controller = getController(request.getMethod(), request.getPath());
        if (controller != null) {
            controller.service(request, response);
            return;
        }
        if (request.getMethod() == HttpMethod.GET) {
            // 파일이 없거나 허용되지 않는 경로이면 forward가 404를 응답합니다.
            response.forward(request.getPath());
            return;
        }
        response.notFound();
    }

    public record Route(HttpMethod method, Path path) {
        public Route(HttpMethod method, String path) {
            this(method, Path.of(path).normalize());
        }
    }
}
