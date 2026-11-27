package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kuit8th.springmvc.model.User;
import kuit8th.springmvc.repository.Repository;
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
    public ResponseEntity<List<String>> users(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();     // 쿠키가 없으면 빈 배열이 아니라 null
        boolean loggedIn = cookies != null && Arrays.stream(cookies)
                .anyMatch(c -> c.getName().equals("logined") && c.getValue().equals("true"));
        if (!loggedIn) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(repository.findAll().stream().map(User::getName).toList());
    }
}