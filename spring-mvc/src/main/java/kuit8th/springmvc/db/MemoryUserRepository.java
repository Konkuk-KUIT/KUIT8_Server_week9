
package kuit8th.springmvc.db;

import kuit8th.springmvc.model.User;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemoryUserRepository implements kuit8th.springmvc.db.Repository {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    public MemoryUserRepository() {
    }

    @Override
    public void addUser(User user) {
        Objects.requireNonNull(user, "user");
        // Reusing an ID replaces the stored user; duplicate-registration validation
        // is not part of this repository's contract.
        users.put(Objects.requireNonNull(user.getUserId(), "userId"), user);
    }

    @Override
    public User findUserById(String userId) {
        return users.get(Objects.requireNonNull(userId, "userId"));
    }

    @Override
    public Collection<User> findAll() {
        // Return a detached, unmodifiable collection rather than a live map view.
        return List.copyOf(users.values());
    }
}
