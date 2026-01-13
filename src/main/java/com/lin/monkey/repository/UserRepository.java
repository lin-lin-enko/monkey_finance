package com.lin.monkey.repository;

import com.lin.monkey.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>{
    User findById(String id);
    User findByUsername(String username);
    User findByEmail(String email);
}