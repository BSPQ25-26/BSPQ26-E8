package com.bspq26e8.backend.user.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import com.bspq26e8.backend.user.service.UserService;
import com.bspq26e8.backend.user.service.UserService.UserView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getUserById(@PathVariable UUID id) {
		Optional<UserView> user = userService.findById(id);

		if (user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("User not found"));
		}

		return ResponseEntity.ok(user.get());
	}

	@GetMapping("/by-email")
	public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
		String normalizedEmail = normalize(email);

		if (!isValidEmail(normalizedEmail)) {
			return ResponseEntity.badRequest().body(error("Invalid email format"));
		}

		Optional<UserView> user = userService.findByEmail(normalizedEmail);

		if (user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("User not found"));
		}

		return ResponseEntity.ok(user.get());
	}

	private boolean isValidEmail(String email) {
		return email != null && SIMPLE_EMAIL_PATTERN.matcher(email).matches();
	}

	private String normalize(String value) {
		return value == null ? null : value.trim();
	}

	private Map<String, String> error(String message) {
		return Map.of("error", message);
	}
}
