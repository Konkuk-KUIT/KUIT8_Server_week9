package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kuit8th.springmvc.db.UserRepository;
import kuit8th.springmvc.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//데이터 전달 일꾼
@RestController
public class UserApiController {

    private final UserRepository userRepository;

    public UserApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<String>> getUsers(HttpServletRequest request) {
        if (!isLogined(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<String> names = userRepository.findAll().stream()
                .map(User::getName)
                .toList();
        return ResponseEntity.ok(names);
    }

    @GetMapping("/api/users/{userId}")
    public ResponseEntity<String> getUser(@PathVariable("userId") String userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user.getName());
    }

    private boolean isLogined(HttpServletRequest request) {
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
/* 
    @GetMapping("/api/redirect-test")
    public String redirectTest() {
        return "redirect:/index.html";
    }
*/
}

