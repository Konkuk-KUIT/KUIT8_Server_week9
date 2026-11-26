package controller;

import db.Repository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;

import java.io.IOException;
import java.util.logging.Logger;

public class SignUpController implements Controller {

    private final Repository repository;

    private static final Logger log =
            Logger.getLogger(SignUpController.class.getName());

    // 이게 바로 생성자 주입!
    //회원가입 컨트롤러는 사용자를 저장할 수 있는 객체가 필요할 뿐,
    //그것이 메모리 저장소인지 MySQL 저장소인지 알 필요가 없음
    public SignUpController(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, HttpResponse response)
            throws IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        if (userId == null || userId.isBlank()
                || password == null || name == null || email == null) {

            response.error(HttpStatus.BAD_REQUEST);
            return;
        }

        repository.addUser(new User(userId, password, name, email));

        log.info("회원가입 성공: " + userId);

        response.redirect("/index.html");
    }
}