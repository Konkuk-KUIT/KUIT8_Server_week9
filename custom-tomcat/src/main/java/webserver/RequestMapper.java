package webserver;

import controller.Controller;
import http.HttpRequest;

import java.util.Map;

public class RequestMapper {
    private final Map<RequestKey, Controller> controllers;
    private final Controller staticFileController;

    public RequestMapper(Map<RequestKey, Controller> controllers, Controller staticFileController) {
        this.controllers = Map.copyOf(controllers);
        this.staticFileController = staticFileController;
    }

    public Controller getController(HttpRequest request) {
        // getPath()에는 쿼리스트링이 제외된 경로가 들어 있다.
        RequestKey key = new RequestKey(request.getMethod(), request.getPath());
        // 매핑이 우선이며, 없으면 정적 파일 존재 여부와 404 처리를 맡긴다.
        return controllers.getOrDefault(key, staticFileController);
    }
}
