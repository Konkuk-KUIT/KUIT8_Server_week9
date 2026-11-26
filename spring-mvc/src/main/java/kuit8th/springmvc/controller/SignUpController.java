package kuit8th.springmvc.controller;

import kuit8th.springmvc.db.Repository;
import kuit8th.springmvc.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignUpController {
    private final Repository repository;

    public SignUpController(Repository repository) {
        this.repository = repository;
    }

    @PostMapping("/user/signup")
    public String signUp(
            @RequestParam("userId") String userId,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("email") String email
    ) {
        User user = new User(userId, password, name, email);
        repository.addUser(user);

        return "redirect:/index.html";
    }
}