
package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kuit8th.springmvc.db.Repository;
import kuit8th.springmvc.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class UserApiController {

    private final Repository repository;

    public UserApiController(Repository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<String>> getUsers(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        boolean isLogined = cookies != null
                && Arrays.stream(cookies)
                .anyMatch(cookie ->
                        cookie.getName().equals("logined")
                        && cookie.getValue().equals("true")
                );

        if (!isLogined) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> names = repository.findAll().stream()
                .map(User::getName)
                .toList();

        return ResponseEntity.ok(names);
    }
}
