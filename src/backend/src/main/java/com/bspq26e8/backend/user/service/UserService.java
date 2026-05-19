package com.bspq26e8.backend.user.service;

import com.bspq26e8.backend.user.LanguageProjection;
import com.bspq26e8.backend.user.entity.User;
import com.bspq26e8.backend.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public UpdateProfileResult updateProfile(UUID userId, UUID authenticatedUserId, String profilePicture, List<Long> preferredLanguageIds) {
        if (!userId.equals(authenticatedUserId)) {
            return UpdateProfileResult.forbidden();
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return UpdateProfileResult.notFound();
        }

        User user = userOpt.get();

        if (profilePicture != null) {
            user.setProfilePicture(profilePicture);
        }

        if (preferredLanguageIds != null) {
            user.setPreferredLanguageIds(new HashSet<>(preferredLanguageIds));
        }

        userRepository.save(user);
        return UpdateProfileResult.updated(toView(user));
    }

    private UserView toView(User user) {
        List<LanguageInfo> languages = List.of();
        if (user.getId() != null) {
            List<LanguageProjection> projections = userRepository.findPreferredLanguagesByUserId(user.getId());
            if (projections != null) {
                languages = projections.stream()
                        .map(p -> new LanguageInfo(p.getId(), p.getName(), p.getCode()))
                        .collect(Collectors.toList());
            }
        }
        return new UserView(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.isActive(),
                user.isEmailVerified(),
                user.getProfilePicture(),
                languages
        );
    }

    public record LanguageInfo(Long id, String name, String code) {
    }

    public record UserView(UUID id, String email, String username, boolean isActive, boolean emailVerified,
                           String profilePicture, List<LanguageInfo> preferredLanguages) {
    }

    public record CreateUserResult(boolean created, UserView user) {

        public static CreateUserResult created(UserView user) {
            return new CreateUserResult(true, user);
        }

        public static CreateUserResult conflict() {
            return new CreateUserResult(false, null);
        }
    }

    public record UpdateProfileResult(Status status, UserView user) {

        public enum Status { UPDATED, NOT_FOUND, FORBIDDEN }

        public static UpdateProfileResult updated(UserView user) {
            return new UpdateProfileResult(Status.UPDATED, user);
        }

        public static UpdateProfileResult notFound() {
            return new UpdateProfileResult(Status.NOT_FOUND, null);
        }

        public static UpdateProfileResult forbidden() {
            return new UpdateProfileResult(Status.FORBIDDEN, null);
        }

        public boolean isUpdated() { return status == Status.UPDATED; }
        public boolean isNotFound() { return status == Status.NOT_FOUND; }
        public boolean isForbidden() { return status == Status.FORBIDDEN; }
    }
}
