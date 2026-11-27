package webserver;

import db.Repository;
import model.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;

final class SignUpController implements Controller {
    private final Repository repository;

    SignUpController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> params = request.form();
        if (!params.keySet().containsAll(List.of("userId", "password", "name", "email"))) {
            return;
        }

        repository.addUser(new User(
                params.get("userId"),
                params.get("password"),
                params.get("name"),
                params.get("email")
        ));
        response.redirect("/index.html");
    }
}
