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
    private final Repository userRepository;

    public UserController(Repository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/user/signup")
    public String signup(
            @RequestParam("userId") String userId,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("email") String email) {
        userRepository.addUser(new User(userId, password, name, email));
        return "redirect:/index.html";
    }

    @PostMapping("/user/login")
    public String login(
            @RequestParam("userId") String userId,
            @RequestParam("password") String password,
            HttpServletResponse response) {
        User user = userRepository.findUserById(userId);

        // 사용자가 없으면 비밀번호 비교를 실행하지 않는다.
        if (user != null && user.getPassword().equals(password)) {
            Cookie cookie = new Cookie("logined", "true");
            cookie.setPath("/");
            response.addCookie(cookie);
            return "redirect:/index.html";
        }

        return "redirect:/user/login_failed.html";
    }
}
