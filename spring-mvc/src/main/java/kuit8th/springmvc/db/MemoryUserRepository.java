package kuit8th.springmvc.db;

import kuit8th.springmvc.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@org.springframework.stereotype.Repository
public class MemoryUserRepository implements Repository {

    private final Map<String, User> users =
            new ConcurrentHashMap<>();

    @Override
    public void addUser(User user) {

        Objects.requireNonNull(user, "user");

        users.put(
                Objects.requireNonNull(user.getUserId(), "userId"),
                user
        );
    }

    @Override
    public User findUserById(String userId) {

        return users.get(
                Objects.requireNonNull(userId, "userId")
        );
    }

    @Override
    public Collection<User> findAll() {

        return List.copyOf(users.values());
    }
}