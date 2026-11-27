package controller;

import http.HttpRequest;
import http.HttpResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileController implements Controller {
    private final Path webRoot = Path.of("webapp").toAbsolutePath().normalize();

    public boolean hasFile(String path) {
        Path file = webRoot.resolve(path.substring(1)).normalize();
        return file.startsWith(webRoot) && Files.isRegularFile(file);
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        response.forward(request.getPath());
    }
}
