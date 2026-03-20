package com.bspq26e8.backend.auth;

import com.bspq26e8.backend.auth.dto.LoginRequest;
import com.bspq26e8.backend.auth.dto.LoginResponse;
import com.bspq26e8.backend.auth.security.JwtProvider;
import com.bspq26e8.backend.user.User;
import com.bspq26e8.backend.user.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    /**
     * Authenticate user with email and password
     * Returns JWT tokens if credentials are valid
     */
    public LoginResponse login(LoginRequest request) throws AuthenticationException {
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(request.email());
        
        if (userOptional.isEmpty()) {
            throw new AuthenticationException("Invalid email or password");
        }

        User user = userOptional.get();

        // Validate password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Generate tokens
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getUsername());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        return new LoginResponse(
            accessToken,
            refreshToken,
            jwtProvider.getExpirationTime()
        );
    }

    /**
     * Refresh access token using refresh token
     */
    public LoginResponse refreshAccessToken(String refreshToken) throws AuthenticationException {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        try {
            var userId = jwtProvider.getUserIdFromToken(refreshToken);
            var userOptional = userRepository.findById(userId);

            if (userOptional.isEmpty()) {
                throw new AuthenticationException("User not found");
            }

            User user = userOptional.get();
            String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getUsername());

            return new LoginResponse(
                newAccessToken,
                refreshToken,
                jwtProvider.getExpirationTime()
            );
        } catch (Exception e) {
            throw new AuthenticationException("Failed to refresh token: " + e.getMessage());
        }
    }
}
