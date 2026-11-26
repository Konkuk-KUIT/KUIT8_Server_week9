package kuit8th.springmvc.controller;

import kuit8th.springmvc.db.Repository;
import kuit8th.springmvc.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 선택 미션. 회원 한 명 조회 API
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
public class UserApiController {

    private final Repository repository;

    public UserApiController(Repository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<String>> findAll(
            HttpServletRequest request) {

        if (!hasLoginCookie(request)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        List<String> names = repository.findAll()
                .stream()
                .map(User::getName)
                .toList();

        return ResponseEntity.ok(names);
    }

    private boolean hasLoginCookie(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return false;
        }

        for (Cookie cookie : cookies) {

            if ("logined".equals(cookie.getName())
                    && "true".equals(cookie.getValue())) {
                return true;
            }
        }

        return false;
    }

    //필수 실험. @RestController에서 redirect를 반환하면?
    @GetMapping("/api/redirect-test")
    public String redirectTest() {
        return "redirect:/index.html";
    }

    // 선택미션
    @GetMapping("/api/users/{userId}")
    public ResponseEntity<String> findUser(
            @PathVariable("userId") String userId,
            HttpServletRequest request) {

        if (!hasLoginCookie(request)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = repository.findUserById(userId);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user.getName());
    }
}
