package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kuit8th.springmvc.model.User;
import kuit8th.springmvc.repository.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final Repository userRepository;

    public UserApiController(Repository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<String>> list(HttpServletRequest request) {
        if (!isLoggedIn(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // User 전체를 반환하지 않고 이름만 반환한다.
        List<String> names = userRepository.findAll().stream()
                .map(User::getName)
                .toList();
        return ResponseEntity.ok(names);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<String> findUser(
            @PathVariable("userId") String userId,
            HttpServletRequest request) {
        if (!isLoggedIn(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user.getName());
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }

        for (Cookie cookie : cookies) {
            if ("logined".equals(cookie.getName()) && "true".equals(cookie.getValue())) {
                return true;
            }
        }
        return false;
    }
}
