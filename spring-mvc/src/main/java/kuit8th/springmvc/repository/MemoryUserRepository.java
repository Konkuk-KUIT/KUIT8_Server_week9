package kuit8th.springmvc.repository;

import kuit8th.springmvc.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 같은 패키지의 Repository 인터페이스와 이름이 겹쳐 전체 이름을 사용한다.
@org.springframework.stereotype.Repository
public class MemoryUserRepository implements Repository {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    @Override
    public User findUserById(String userId) {
        return users.get(userId);
    }

    @Override
    public Collection<User> findAll() {
        return List.copyOf(users.values());
    }
}
