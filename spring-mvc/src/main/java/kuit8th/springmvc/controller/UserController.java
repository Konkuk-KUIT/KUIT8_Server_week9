package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kuit8th.springmvc.db.UserRepository;
import kuit8th.springmvc.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

//화면 담당 일꾼
@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //동작 종류(일꾼 종류) 올리기
    @PostMapping("/user/signup")
    public String signUp(@RequestParam("userId") String userId,
                         @RequestParam("password") String password,
                         @RequestParam("name") String name,
                         @RequestParam("email") String email) {
        userRepository.addUser(new User(userId, password, name, email));
        return "redirect:/index.html";
    }

    @PostMapping("/user/login")
    public String login(@RequestParam("userId") String userId,
                        @RequestParam("password") String password,
                        HttpServletResponse response) {
        User user = userRepository.findUserById(userId);
        if (user == null || !user.getPassword().equals(password)) {
            return "redirect:/user/login_failed.html";
        }
        Cookie cookie = new Cookie("logined", "true");
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/index.html";
    }
}