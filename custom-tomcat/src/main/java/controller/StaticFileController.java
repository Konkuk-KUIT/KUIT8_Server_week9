package controller;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class StaticFileController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        String requestPath = request.startLine().path();

        if (request.startLine().method() != HttpMethod.GET
                || !requestPath.startsWith("/")) {
            response.notFound();
            return;
        }

        Path file;
        try {
            Path webapp = Path.of("webapp").toRealPath();

            file = webapp.resolve(requestPath.substring(1)).toRealPath();

            if (!file.startsWith(webapp)
                    || file.equals(webapp.resolve("user").resolve("list.html"))
                    || !Files.isRegularFile(file)) {
                response.notFound();
                return;
            }
        } catch (IOException | InvalidPathException e) {
            response.notFound();
            return;
        }

        response.forward(file);
    }
}
