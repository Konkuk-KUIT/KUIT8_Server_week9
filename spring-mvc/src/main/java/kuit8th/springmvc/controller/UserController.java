package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kuit8th.springmvc.db.UserRepository;
import kuit8th.springmvc.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController{

    private final UserRepository repository;
    public UserController(UserRepository repository){
        this.repository = repository;
    }

    @PostMapping("/user/signup")
    public String signUp(@RequestParam String userId,            // ③
                         @RequestParam String password,
                         @RequestParam String name,
                         @RequestParam String email) {
        repository.addUser(new User(userId, password, name, email));
        return "redirect:/index.html";                           // ④
    }

    @PostMapping("/user/login")
    public String login(@RequestParam String userId, @RequestParam String password, HttpServletResponse response){
        User findedUser = repository.findUserById(userId);
        if(findedUser != null &&findedUser.getPassword().equals(password)){
            //찾은 유저에 대한 비밀번호가 일치할 때
            Cookie cookie = new Cookie("logined", "true");
            cookie.setPath("/");
            response.addCookie(cookie);
            return "redirect:/index.html";
        }else {
            //없는 아이디 혹은 틀린 비밀번호 일 때
            return "redirect:/user/login_failed.html";
        }
    }
}
