package com.bspq26e8.backend.user.repository;

import java.util.Optional;
import java.util.UUID;

import com.bspq26e8.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
}
