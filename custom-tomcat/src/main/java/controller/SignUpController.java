package controller;

import db.MemoryUserRepository;
import db.Repository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import http.util.HttpRequestUtils;
import model.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class SignUpController implements Controller {
    private final Repository repository;

    public SignUpController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        HttpMethod method = request.startLine().method();
        Map<String, String> params;

        if (method == HttpMethod.GET) {
            params = HttpRequestUtils.parseQueryParameter(request.startLine().query());
        } else if (method == HttpMethod.POST) {
            if (request.body().isEmpty()) {
                return;
            }
            params = request.formParams();
        } else {
            return;
        }

        User user = new User(
                params.get("userId"),
                params.get("password"),
                params.get("name"),
                params.get("email")
        );
        repository.addUser(user);

        if (method == HttpMethod.POST) {
            response.redirect("/index.html");
        }
    }
}
