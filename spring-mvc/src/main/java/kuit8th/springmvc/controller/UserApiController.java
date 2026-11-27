package kuit8th.springmvc.controller;

import kuit8th.springmvc.db.Repository;
import kuit8th.springmvc.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserApiController {

    private final Repository repository;

    public UserApiController(Repository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<String>> getUsers(
            HttpServletRequest request) {

        if (!isLoggedIn(request)) {
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

    private boolean isLoggedIn(HttpServletRequest request) {
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
}