package com.bspq26e8.backend.auth.service;

import com.bspq26e8.backend.common.AccessTokenService;
import com.bspq26e8.backend.user.entity.RefreshToken;
import com.bspq26e8.backend.user.entity.User;
import com.bspq26e8.backend.user.repository.RefreshTokenRepository;
import com.bspq26e8.backend.user.repository.UserRepository;
import com.bspq26e8.backend.user.service.UserService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int REFRESH_TOKEN_DAYS = 30;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final AccessTokenService accessTokenService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            UserService userService,
            AccessTokenService accessTokenService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
        this.accessTokenService = accessTokenService;
    }

    public RegisterResult register(String email, String username, String password) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedUsername = normalize(username);
        String normalizedPassword = normalize(password);

        if (!isValidEmail(normalizedEmail)) {
            return RegisterResult.invalid("Invalid email format");
        }

        if (!userService.isValidUsername(normalizedUsername)) {
            return RegisterResult.invalid("Invalid username");
        }

        if (!isValidPassword(normalizedPassword)) {
            return RegisterResult.invalid("Password must be at least 6 characters long");
        }

        if (userRepository.existsByEmail(normalizedEmail) || userRepository.existsByUsername(normalizedUsername)) {
            return RegisterResult.conflict("A user with that email or username already exists");
        }

        String passwordHash = hashPassword(normalizedPassword);
        User user = userRepository.save(new User(normalizedEmail, normalizedUsername, passwordHash));

        return RegisterResult.created(new AuthUserView(user.getId(), user.getEmail(), user.getUsername()));
    }

    public LoginResult login(String email, String password, String ipAddress, String userAgent) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedPassword = normalize(password);

        if (!isValidEmail(normalizedEmail)) {
            return LoginResult.invalid("Invalid email format");
        }

        if (!isValidPassword(normalizedPassword)) {
            return LoginResult.invalid("Password is required");
        }

        Optional<User> maybeUser = userRepository.findByEmail(normalizedEmail);
        if (maybeUser.isEmpty()) {
            return LoginResult.unauthorized("Invalid credentials");
        }

        User user = maybeUser.get();
        if (!passwordMatches(normalizedPassword, user.getPasswordHash())) {
            return LoginResult.unauthorized("Invalid credentials");
        }

        SessionTokens tokens = createSessionTokens(user, ipAddress, userAgent);

        return LoginResult.success(
                tokens,
                new AuthUserView(user.getId(), user.getEmail(), user.getUsername())
        );
    }

    public RefreshResult refresh(String refreshToken) {
        String normalizedRefreshToken = normalize(refreshToken);

        if (normalizedRefreshToken == null || normalizedRefreshToken.isBlank()) {
            return RefreshResult.invalid("refreshToken is required");
        }

        String hashedRefreshToken = hashToken(normalizedRefreshToken);
        Optional<RefreshToken> maybeToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hashedRefreshToken);

        if (maybeToken.isEmpty()) {
            return RefreshResult.unauthorized("Invalid refresh token");
        }

        RefreshToken storedToken = maybeToken.get();

        if (storedToken.isExpired(OffsetDateTime.now())) {
            storedToken.revokeNow();
            refreshTokenRepository.save(storedToken);
            return RefreshResult.unauthorized("Refresh token expired");
        }

        storedToken.revokeNow();
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        SessionTokens newTokens = createSessionTokens(user, storedToken.getIpAddress(), storedToken.getUserAgent());

        return RefreshResult.success(newTokens, new AuthUserView(user.getId(), user.getEmail(), user.getUsername()));
    }

    public LogoutResult logout(String refreshToken) {
        String normalizedRefreshToken = normalize(refreshToken);

        if (normalizedRefreshToken == null || normalizedRefreshToken.isBlank()) {
            return LogoutResult.invalid("refreshToken is required");
        }

        String hashedRefreshToken = hashToken(normalizedRefreshToken);
        Optional<RefreshToken> maybeToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hashedRefreshToken);

        if (maybeToken.isEmpty()) {
            return LogoutResult.unauthorized("Invalid refresh token");
        }

        RefreshToken storedToken = maybeToken.get();
        storedToken.revokeNow();
        refreshTokenRepository.save(storedToken);

        return LogoutResult.success();
    }

    private SessionTokens createSessionTokens(User user, String ipAddress, String userAgent) {
        String accessToken = accessTokenService.generateAccessToken(user.getId());
        String refreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = hashToken(refreshToken);

        OffsetDateTime expiresAt = OffsetDateTime.now().plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS);
        RefreshToken session = new RefreshToken(user, hashedRefreshToken, expiresAt, ipAddress, userAgent);
        refreshTokenRepository.save(session);

        return new SessionTokens("Bearer", accessToken, refreshToken);
    }

    private String hashToken(String rawToken) {
        return hashPassword(rawToken);
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    private boolean passwordMatches(String rawPassword, String storedPasswordHash) {
        String hashedRawPassword = hashPassword(rawPassword);
        // Allow pre-existing plain/hash values already stored during development.
        return storedPasswordHash.equals(hashedRawPassword) || storedPasswordHash.equals(rawPassword);
    }

    private boolean isValidEmail(String email) {
        return email != null && SIMPLE_EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    private String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    public record AuthUserView(UUID id, String email, String username) {
    }

    public record RegisterResult(boolean created, boolean invalid, String errorMessage, AuthUserView user) {

        public static RegisterResult created(AuthUserView user) {
            return new RegisterResult(true, false, null, user);
        }

        public static RegisterResult conflict(String message) {
            return new RegisterResult(false, false, message, null);
        }

        public static RegisterResult invalid(String message) {
            return new RegisterResult(false, true, message, null);
        }
    }

    public record LoginResult(
            boolean authenticated,
            boolean invalid,
            String errorMessage,
            SessionTokens tokens,
            AuthUserView user
    ) {

        public static LoginResult success(SessionTokens tokens, AuthUserView user) {
            return new LoginResult(true, false, null, tokens, user);
        }

        public static LoginResult unauthorized(String message) {
            return new LoginResult(false, false, message, null, null);
        }

        public static LoginResult invalid(String message) {
            return new LoginResult(false, true, message, null, null);
        }
    }

    public record RefreshResult(
            boolean refreshed,
            boolean invalid,
            String errorMessage,
            SessionTokens tokens,
            AuthUserView user
    ) {

        public static RefreshResult success(SessionTokens tokens, AuthUserView user) {
            return new RefreshResult(true, false, null, tokens, user);
        }

        public static RefreshResult unauthorized(String message) {
            return new RefreshResult(false, false, message, null, null);
        }

        public static RefreshResult invalid(String message) {
            return new RefreshResult(false, true, message, null, null);
        }
    }

    public record LogoutResult(boolean loggedOut, boolean invalid, String errorMessage) {

        public static LogoutResult success() {
            return new LogoutResult(true, false, null);
        }

        public static LogoutResult unauthorized(String message) {
            return new LogoutResult(false, false, message);
        }

        public static LogoutResult invalid(String message) {
            return new LogoutResult(false, true, message);
        }
    }

    public record SessionTokens(String tokenType, String accessToken, String refreshToken) {
    }
}
