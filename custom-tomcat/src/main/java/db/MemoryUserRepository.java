package db;

import model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryUserRepository implements Repository{
    private static final MemoryUserRepository INSTANCE = new MemoryUserRepository();
    private final Map<String, User> users = new ConcurrentHashMap<>();

    private MemoryUserRepository() {
    }

    public static MemoryUserRepository getInstance() {
        return INSTANCE;
    }

    public void addUser(User user) {
        Objects.requireNonNull(user, "user");
        // Reusing an ID replaces the stored user; duplicate-registration validation
        // is not part of this repository's contract.
        users.put(Objects.requireNonNull(user.getUserId(), "userId"), user);
    }

    public User findUserById(String userId) {
        return users.get(Objects.requireNonNull(userId, "userId"));
    }

    public Collection<User> findAll() {
        // Return a detached, unmodifiable collection rather than a live map view.
        return List.copyOf(users.values());
    }
}
