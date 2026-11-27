package webserver;

import java.io.IOException;
import java.nio.file.Path;

final class HomeController implements Controller {
    private final Path indexPage;

    HomeController(Path indexPage) {
        this.indexPage = indexPage;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        response.forward(indexPage);
    }
}
