package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kuit8th.springmvc.db.UserRepository;
import kuit8th.springmvc.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class UserApiController {
    private final UserRepository repository;

    public UserApiController(UserRepository repository){
        this.repository = repository;
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<String>> users(HttpServletRequest request){
        if(!isLogined(request)){
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<String> names = repository.findAll().stream().map(User::getName).toList();
        return ResponseEntity.ok(names);
    }

    @GetMapping("/api/users/{userId}")
    public ResponseEntity<String> user(@PathVariable String userId){
        User user = repository.findUserById(userId);
        if(user == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user.getName());
    }

    private boolean isLogined(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return false;
        }
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("logined") && cookie.getValue().equals("true")){
                return true;
            }
        }
        return false;
    }




}
