package com.bspq26e8.backend.auth.controller;

import com.bspq26e8.backend.auth.service.AuthService;
import com.bspq26e8.backend.auth.service.AuthService.LoginResult;
import com.bspq26e8.backend.auth.service.AuthService.LogoutResult;
import com.bspq26e8.backend.auth.service.AuthService.RefreshResult;
import com.bspq26e8.backend.auth.service.AuthService.RegisterResult;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody(required = false) RegisterRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(error("Request body is required"));
        }

        RegisterResult result = authService.register(request.email(), request.username(), request.password());

        if (result.invalid()) {
            return ResponseEntity.badRequest().body(error(result.errorMessage()));
        }

        if (!result.created()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error(result.errorMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result.user());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody(required = false) LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        if (request == null) {
            return ResponseEntity.badRequest().body(error("Request body is required"));
        }

        LoginResult result = authService.login(
                request.email(),
                request.password(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        if (result.invalid()) {
            return ResponseEntity.badRequest().body(error(result.errorMessage()));
        }

        if (!result.authenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(result.errorMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "tokenType", result.tokens().tokenType(),
                "accessToken", result.tokens().accessToken(),
                "refreshToken", result.tokens().refreshToken(),
                "user", result.user()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody(required = false) RefreshRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(error("Request body is required"));
        }

        RefreshResult result = authService.refresh(request.refreshToken());

        if (result.invalid()) {
            return ResponseEntity.badRequest().body(error(result.errorMessage()));
        }

        if (!result.refreshed()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(result.errorMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "tokenType", result.tokens().tokenType(),
                "accessToken", result.tokens().accessToken(),
                "refreshToken", result.tokens().refreshToken(),
                "user", result.user()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) LogoutRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(error("Request body is required"));
        }

        LogoutResult result = authService.logout(request.refreshToken());

        if (result.invalid()) {
            return ResponseEntity.badRequest().body(error(result.errorMessage()));
        }

        if (!result.loggedOut()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(result.errorMessage()));
        }

        return ResponseEntity.noContent().build();
    }

    private Map<String, String> error(String message) {
        return Map.of("error", message);
    }

    public record RegisterRequest(String email, String username, String password) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record LogoutRequest(String refreshToken) {
    }
}
