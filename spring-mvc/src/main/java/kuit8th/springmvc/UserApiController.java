package kuit8th.springmvc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class UserApiController {
    private final MemoryUserRepository repository;

    UserApiController(MemoryUserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/users")
    ResponseEntity<List<String>> findUsers(
            @CookieValue(name = "logined", defaultValue = "false") String logined
    ) {
        if (!"true".equals(logined)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(repository.findAllNames());
    }
}
