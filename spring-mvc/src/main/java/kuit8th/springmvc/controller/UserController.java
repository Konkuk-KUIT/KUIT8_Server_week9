
package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kuit8th.springmvc.db.Repository;
import kuit8th.springmvc.model.User;
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
    public String signup(
            @RequestParam String userId,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String email) {

        User user = new User(userId, password, name, email);
        repository.addUser(user);

        return "redirect:/index.html";
    }

    @PostMapping("/user/login")
    public String login(
            @RequestParam String userId,
            @RequestParam String password,
            HttpServletResponse response) {

        User user = repository.findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            Cookie cookie = new Cookie("logined", "true");
            cookie.setPath("/");
            response.addCookie(cookie);

            return "redirect:/index.html";
        }

        return "redirect:/user/login_failed.html";
    }
}
