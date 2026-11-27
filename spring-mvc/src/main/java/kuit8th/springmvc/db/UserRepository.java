package kuit8th.springmvc.db;

import kuit8th.springmvc.model.User;

import java.util.Collection;

public interface UserRepository {
    void addUser(User user);
    User findUserById(String userId);
    Collection<User> findAll();
}
