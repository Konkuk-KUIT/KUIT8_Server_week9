package kuit8th.springmvc;

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
    public String signUp(@RequestParam("userId") String userId,
                         @RequestParam("password") String password,
                         @RequestParam("name") String name,
                         @RequestParam("email") String email) {
        repository.addUser(new User(userId, password, name, email));
        return "redirect:/index.html";
    }

    @PostMapping("/user/login")
    public String login(@RequestParam("userId") String userId,
                        @RequestParam("password") String password,
                        HttpServletResponse response) {
        User user = repository.findUserById(userId);
        if (user == null || !user.getPassword().equals(password)) {
            return "redirect:/user/login_failed.html";
        }

        Cookie cookie = new Cookie("logined", "true");
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/index.html";
    }
}
