package webserver;

import db.Repository;
import model.User;

import java.io.IOException;

final class LoginController implements Controller {
    private final Repository repository;

    LoginController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        String userId = request.form().get("userId");
        String password = request.form().get("password");
        User user = userId == null ? null : repository.findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response.redirect("/index.html", "logined=true; Path=/");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}
