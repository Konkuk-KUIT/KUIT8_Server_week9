package controller;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpMethod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class StaticFileController implements Controller {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        // 정적 파일은 GET 요청으로만 제공한다.
        if (request.getMethod() != HttpMethod.GET) {
            response.notFound();
            return;
        }

        Path webRoot = Path.of("webapp").toAbsolutePath().normalize();
        Path file;
        try {
            file = Path.of("webapp" + request.getPath()).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            response.notFound();
            return;
        }

        // webapp 밖의 경로, 디렉터리, 존재하지 않는 파일은 응답하지 않는다.
        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            response.notFound();
            return;
        }

        response.forward(request.getPath());
    }
}
