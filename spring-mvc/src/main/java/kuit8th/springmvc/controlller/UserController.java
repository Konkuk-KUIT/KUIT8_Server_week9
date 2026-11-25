package kuit8th.springmvc.controlller;

import kuit8th.model.User;
import kuit8th.springmvc.dto.UserDTO;
import kuit8th.springmvc.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/user/signup")
    public ResponseEntity<Void> signUp(
            @ModelAttribute UserDTO userDTO
    ) {
        userRepository.addUser(userDTO);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create("/"))
                .build();
    }

    @PostMapping("/user/login")
    public ResponseEntity<Void> login(
            @RequestParam("userId") String userId,
            @RequestParam("password") String password
    ){
        Optional<User> findUser = userRepository.findUserById(userId);

        // REST -> 401, HTML form -> 302
        if (findUser.isEmpty() || !findUser.get().getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/user/login_fail.html"))
                    .build();
        }

        ResponseCookie cookie = ResponseCookie.from("login-success", "true")
                .path("/")
                .httpOnly(true)
                .maxAge(60) // 1시간 (단위: 초)
                .build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/"))
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/api/user/{userId}")
    public ResponseEntity<User> findUserList(
            @PathVariable String userId
    ){
        Optional<User> findUser = userRepository.findUserById(userId);

        return findUser
                .map(user -> ResponseEntity.status(HttpStatus.OK)
                                .body(user))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("/"))
                        .build());
    }

    @GetMapping("/user/userList")
    public ResponseEntity<List<User>> findUserList(
            @CookieValue(value = "login-success", defaultValue = "false") boolean isLogin
    ){
        if(!isLogin){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(userRepository.findAll().stream().toList());
    }
}
