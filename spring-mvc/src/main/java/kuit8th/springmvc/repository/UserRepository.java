package kuit8th.springmvc.repository;

import kuit8th.model.User;
import kuit8th.springmvc.dto.UserDTO;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    Optional<User> addUser(UserDTO userDTO);
    Optional<User> findUserById(String id);
    Collection<User> findAll();
}
