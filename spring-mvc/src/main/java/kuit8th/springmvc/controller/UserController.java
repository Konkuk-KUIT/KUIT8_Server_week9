package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kuit8th.springmvc.model.User;
import kuit8th.springmvc.repository.Repository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    private final Repository repository;

    public UserController(Repository repository) {
        this.repository = repository;
    }

    @PostMapping("/user/signup")
    public String signup(@RequestParam String userId, @RequestParam String password,
                         @RequestParam String name, @RequestParam String email) {
        repository.addUser(new User(userId, password, name, email));
        return "redirect:/index.html";
    }

    @PostMapping("/user/login")
    public String login(@RequestParam String userId, @RequestParam String password,
                        HttpServletResponse response) {
        User user = repository.findUserById(userId);
        if (user != null && user.getPassword().equals(password)) {
            Cookie cookie = new Cookie("logined", "true");
            cookie.setPath("/");                 // 빼면 쿠키가 /user 에만 전송돼서 /api/users 에 안 갑니다
            response.addCookie(cookie);
            return "redirect:/index.html";
        }
        return "redirect:/user/login_failed.html";
    }
}
