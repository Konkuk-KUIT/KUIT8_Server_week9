package kuit8th.springmvc;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
class MemoryUserRepository {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    void addUser(User user) {
        users.put(user.userId(), user);
    }

    User findUserById(String userId) {
        return users.get(userId);
    }

    List<String> findAllNames() {
        return users.values().stream()
                .map(User::name)
                .toList();
    }
}
