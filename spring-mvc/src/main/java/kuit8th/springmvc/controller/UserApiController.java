package kuit8th.springmvc.controller;

import kuit8th.springmvc.db.UserRepository;
import kuit8th.springmvc.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserApiController {
    private final UserRepository repository;

    public UserApiController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<String>> users(
            @CookieValue(value = "logined", required = false) String logined) {
        if (!"true".equals(logined)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<String> names = repository.findAll().stream()
                .map(User::getName)
                .toList();
        return ResponseEntity.ok(names);
    }
}
