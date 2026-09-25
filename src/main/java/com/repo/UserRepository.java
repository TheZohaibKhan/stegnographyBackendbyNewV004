package com.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByUsername(String username);
    long countByRole(String role);
    List<User> findByRole(String role);
}
