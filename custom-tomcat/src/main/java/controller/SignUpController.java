package controller;

import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import java.io.IOException;

public class SignUpController implements Controller {
    private final Repository userRepository;

    public SignUpController(Repository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email"));

        userRepository.addUser(user);
        response.redirect("/index.html");
    }
}
