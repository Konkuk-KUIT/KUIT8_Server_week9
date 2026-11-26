package controller;

import java.io.IOException;
import http.HttpRequest;
import http.HttpResponse;
import java.nio.file.Path;

public class HomeController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        response.forward(Path.of("webapp", "index.html"));
    }
}
