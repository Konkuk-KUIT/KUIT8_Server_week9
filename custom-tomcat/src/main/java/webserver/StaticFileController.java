package webserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

final class StaticFileController implements Controller {
    private final Path webRoot;
    private final Path protectedPage;

    StaticFileController(Path webRoot, Path protectedPage) throws IOException {
        this.webRoot = webRoot.toRealPath();
        this.protectedPage = protectedPage.toRealPath();
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        Path file = findFile(RequestKey.from(request));
        if (file == null) {
            response.notFound();
            return;
        }
        response.forward(file);
    }

    private Path findFile(RequestKey key) throws IOException {
        if (key.method() != HttpMethod.GET || !key.path().startsWith("/")) {
            return null;
        }

        Path file;
        try {
            file = webRoot.resolve(key.path().substring(1)).normalize();
        } catch (InvalidPathException e) {
            return null;
        }
        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            return null;
        }

        Path realFile = file.toRealPath();
        if (!realFile.startsWith(webRoot) || Files.isSameFile(realFile, protectedPage)) {
            return null;
        }
        return realFile;
    }
}
