package com.bspq26e8.backend.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Validate username format
     * Must be 3-30 characters and contain only letters, numbers, and underscores
     */
    public boolean isValidUsername(String username) {
        
        if (username == null)
            return false;

        String trimmed = username.trim();

        if (trimmed.length() < 3 || trimmed.length() > 30)
            return false;

        return trimmed.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Create a new user with encrypted password
     */
    public User createUser(String email, String username, String password) {
        String encryptedPassword = passwordEncoder.encode(password);
        User user = new User(email, username, encryptedPassword);
        return userRepository.save(user);
    }
}
