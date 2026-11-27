package webserver;

import java.io.IOException;
import java.nio.file.Path;

final class ListController implements Controller {
    private final Path listPage;

    ListController(Path listPage) {
        this.listPage = listPage;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        if (!isLoggedIn(request.header("Cookie"))) {
            response.redirect("/user/login.html");
            return;
        }
        response.forward(listPage);
    }

    private boolean isLoggedIn(String cookieHeader) {
        if (cookieHeader == null) {
            return false;
        }

        for (String cookie : cookieHeader.split(";")) {
            if (cookie.trim().equals("logined=true")) {
                return true;
            }
        }

        return false;
    }
}
