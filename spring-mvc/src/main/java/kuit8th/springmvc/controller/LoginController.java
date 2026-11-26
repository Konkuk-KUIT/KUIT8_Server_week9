package kuit8th.springmvc.controller;

import kuit8th.springmvc.db.Repository;
import kuit8th.springmvc.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final Repository repository;

    public LoginController(Repository repository) {
        this.repository = repository;
    }

    @PostMapping("/user/login")
    public String login(
            @RequestParam("userId") String userId,
            @RequestParam("password") String password,
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