package kuit8th.springmvc;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
class UserController {
    private final MemoryUserRepository repository;

    UserController(MemoryUserRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/signup")
    String signup(
            @RequestParam("userId") String userId,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("email") String email
    ) {
        repository.addUser(new User(userId, password, name, email));
        return "redirect:/index.html";
    }

    @PostMapping("/login")
    String login(
            @RequestParam("userId") String userId,
            @RequestParam("password") String password,
            HttpServletResponse response
    ) {
        User user = repository.findUserById(userId);
        if (user == null || !password.equals(user.password())) {
            return "redirect:/user/login_failed.html";
        }

        Cookie cookie = new Cookie("logined", "true");
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/index.html";
    }
}
