package com.bspq26e8.backend.user.controller;

import com.bspq26e8.backend.common.AccessTokenService;
import com.bspq26e8.backend.user.service.UserService;
import com.bspq26e8.backend.user.service.UserService.UpdateProfileResult;
import com.bspq26e8.backend.user.service.UserService.UserView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	private final UserService userService;
	private final AccessTokenService accessTokenService;

	public UserController(UserService userService, AccessTokenService accessTokenService) {
		this.userService = userService;
		this.accessTokenService = accessTokenService;
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

	@PutMapping("/{id}/profile")
	public ResponseEntity<?> updateProfile(
			@PathVariable UUID id,
			@RequestBody(required = false) UpdateProfileRequest request,
			HttpServletRequest httpRequest
	) {
		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		if (request == null) {
			return ResponseEntity.badRequest().body(error("Request body is required"));
		}

		UpdateProfileResult result = userService.updateProfile(
				id,
				authenticatedUserId.get(),
				request.profilePicture(),
				request.preferredLanguageIds()
		);

		if (result.isForbidden()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error("Cannot modify another user's profile"));
		}

		if (result.isNotFound()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("User not found"));
		}

		return ResponseEntity.ok(result.user());
	}

	private Optional<UUID> authenticatedUserId(HttpServletRequest httpRequest) {
		String authorization = httpRequest.getHeader("Authorization");
		return accessTokenService.extractUserIdFromAuthorizationHeader(authorization);
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

	public record UpdateProfileRequest(String profilePicture, List<Long> preferredLanguageIds) {
	}
}
