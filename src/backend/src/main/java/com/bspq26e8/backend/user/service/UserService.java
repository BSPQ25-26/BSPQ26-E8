package com.bspq26e8.backend.user.service;

import java.util.Optional;
import java.util.UUID;

import com.bspq26e8.backend.user.entity.User;
import com.bspq26e8.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isValidUsername(String username) {
        
        if (username == null)
            return false;

        String trimmed = username.trim();

        if (trimmed.length() < 3 || trimmed.length() > 30)
            return false;

        return trimmed.matches("^[a-zA-Z0-9_]+$");
    }

    public CreateUserResult createUser(String email, String username, String passwordHash) {
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedUsername = username.trim();

        if (userRepository.existsByEmail(normalizedEmail) || userRepository.existsByUsername(normalizedUsername)) {
            return CreateUserResult.conflict();
        }

        User user = new User(normalizedEmail, normalizedUsername, passwordHash.trim());
        User savedUser = userRepository.save(user);

        return CreateUserResult.created(toView(savedUser));
    }

    public Optional<UserView> findById(UUID id) {
        return userRepository.findById(id).map(this::toView);
    }

    public Optional<UserView> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase()).map(this::toView);
    }

    private UserView toView(User user) {
        return new UserView(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.isActive(),
                user.isEmailVerified()
        );
    }

    public record UserView(UUID id, String email, String username, boolean isActive, boolean emailVerified) {
    }

    public record CreateUserResult(boolean created, UserView user) {

        public static CreateUserResult created(UserView user) {
            return new CreateUserResult(true, user);
        }

        public static CreateUserResult conflict() {
            return new CreateUserResult(false, null);
        }
    }
}
