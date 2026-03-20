package com.bspq26e8.backend.user;

import com.bspq26e8.backend.user.dto.UserCreateRequest;
import com.bspq26e8.backend.user.dto.UserResponse;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/users/{id}
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
            .map(user -> ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getUsername())))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/users/username/{username}
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
            .map(user -> ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getUsername())))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/users
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
        // Validate username
        if (!userService.isValidUsername(request.username())) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid username. Must be 3-30 characters and contain only letters, numbers, and underscores"
            ));
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Email already in use"
            ));
        }

        // Check if username already exists
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Username already in use"
            ));
        }

        // Create user
        User user = userService.createUser(request.email(), request.username(), request.password());
        UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
