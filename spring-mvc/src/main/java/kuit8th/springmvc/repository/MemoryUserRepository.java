package kuit8th.springmvc.repository;

import kuit8th.model.User;
import kuit8th.springmvc.dto.UserDTO;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public Optional<User> addUser(UserDTO userDto) {
        User newUser = new User(userDto.userId(), userDto.password(), userDto.name(), userDto.email());
        users.put(Objects.requireNonNull(newUser.getUserId(), "userId"), newUser);

        return Optional.of(newUser);
    }

    public Optional<User> findUserById(String userId) {
        User findUser = users.get(userId);
        return Optional.ofNullable(findUser);
    }

    public Collection<User> findAll() {
        return List.copyOf(users.values());
    }
}
