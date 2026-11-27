package webserver;

import java.io.IOException;
import java.util.Map;

final class RequestMapper {
    private final Map<RequestKey, Controller> routes;
    private final Controller staticFiles;

    RequestMapper(Map<RequestKey, Controller> routes, Controller staticFiles) {
        this.routes = Map.copyOf(routes);
        this.staticFiles = staticFiles;
    }

    Controller findController(HttpRequest request) {
        return routes.get(RequestKey.from(request));
    }

    void dispatch(HttpRequest request, HttpResponse response) throws IOException {
        Controller controller = findController(request);
        if (controller == null) {
            controller = staticFiles;
        }
        controller.execute(request, response);
    }
}
