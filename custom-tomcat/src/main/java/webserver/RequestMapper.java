
package webserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import controller.Controller;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;

public class RequestMapper {

    private final Map<RequestKey, Controller> mappings = new HashMap<>();
    private final Controller staticFileController;

    public RequestMapper(Controller staticFileController) {
        this.staticFileController = staticFileController;
    }

    // HTTP Method와 경로를 기준으로 Controller 등록
    public void register(
            HttpMethod method,
            String path,
            Controller controller) {

        mappings.put(new RequestKey(method, path), controller);
    }

    // 요청에 맞는 Controller 탐색
    public Controller findController(HttpMethod method, String path) {
        return mappings.get(new RequestKey(method, path));
    }

    // 요청을 적절한 Controller에 전달
    public void dispatch(
            HttpRequest request,
            HttpResponse response) throws IOException {

        Controller controller = findController(
                request.getMethod(),
                request.getPath()
        );

        // 1. 매핑된 요청 처리
        if (controller != null) {
            controller.execute(request, response);
            return;
        }

        // 2. 매핑되지 않은 GET 요청은 정적 파일 처리
        if (request.getMethod() == HttpMethod.GET) {
            staticFileController.execute(request, response);
            return;
        }

        // 3. 처리할 수 없는 요청은 404 반환
        response.error(HttpStatus.NOT_FOUND);
    }
}
