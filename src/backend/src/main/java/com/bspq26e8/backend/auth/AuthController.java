package com.bspq26e8.backend.auth;

import com.bspq26e8.backend.auth.dto.LoginRequest;
import com.bspq26e8.backend.auth.dto.LoginResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /api/auth/login
     * Login with email and password
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Validate request
            if (request.email() == null || request.email().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email is required"
                ));
            }

            if (request.password() == null || request.password().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Password is required"
                ));
            }

            // Authenticate user
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/auth/refresh
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");

            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Refresh token is required"
                ));
            }

            // Refresh token
            LoginResponse response = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
