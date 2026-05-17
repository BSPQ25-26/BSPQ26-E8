package com.bspq26e8.backend.unit.auth.service;

import com.bspq26e8.backend.auth.security.AccessTokenService;
import com.bspq26e8.backend.auth.service.AuthService;
import com.bspq26e8.backend.user.entity.RefreshToken;
import com.bspq26e8.backend.user.entity.User;
import com.bspq26e8.backend.user.repository.RefreshTokenRepository;
import com.bspq26e8.backend.user.repository.UserRepository;
import com.bspq26e8.backend.user.service.UserService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private AccessTokenService accessTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerReturnsInvalidWhenEmailFormatIsWrong() {
        AuthService.RegisterResult result = authService.register("invalid-email", "alice", "secret1");

        assertFalse(result.created());
        assertTrue(result.invalid());
        assertEquals("Invalid email format", result.errorMessage());
        verify(userService, never()).isValidUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerReturnsInvalidWhenUsernameIsNotValid() {
        when(userService.isValidUsername("alice")).thenReturn(false);

        AuthService.RegisterResult result = authService.register("alice@example.com", "alice", "secret1");

        assertFalse(result.created());
        assertTrue(result.invalid());
        assertEquals("Invalid username", result.errorMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerReturnsConflictWhenEmailOrUsernameAlreadyExists() {
        when(userService.isValidUsername("alice")).thenReturn(true);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        AuthService.RegisterResult result = authService.register("alice@example.com", "alice", "secret1");

        assertFalse(result.created());
        assertFalse(result.invalid());
        assertEquals("A user with that email or username already exists", result.errorMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerCreatesUserAndNormalizesInputValues() {
        UUID userId = UUID.randomUUID();
        User savedUser = org.mockito.Mockito.mock(User.class);

        when(userService.isValidUsername("Alice")).thenReturn(true);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("Alice")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(savedUser.getId()).thenReturn(userId);
        when(savedUser.getEmail()).thenReturn("alice@example.com");
        when(savedUser.getUsername()).thenReturn("Alice");

        AuthService.RegisterResult result = authService.register("  ALICE@EXAMPLE.COM ", "  Alice ", " secret1 ");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persistedUser = userCaptor.getValue();

        assertEquals("alice@example.com", persistedUser.getEmail());
        assertEquals("Alice", persistedUser.getUsername());
        assertNotNull(persistedUser.getPasswordHash());
        assertEquals(64, persistedUser.getPasswordHash().length());

        assertTrue(result.created());
        assertNotNull(result.user());
        assertEquals(userId, result.user().id());
    }

    @Test
    void loginReturnsUnauthorizedWhenUserIsNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        AuthService.LoginResult result = authService.login("missing@example.com", "secret1", "127.0.0.1", "JUnit");

        assertFalse(result.authenticated());
        assertFalse(result.invalid());
        assertEquals("Invalid credentials", result.errorMessage());
    }

    @Test
    void loginReturnsUnauthorizedWhenPasswordDoesNotMatch() {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getPasswordHash()).thenReturn("different-password");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        AuthService.LoginResult result = authService.login("alice@example.com", "secret1", "127.0.0.1", "JUnit");

        assertFalse(result.authenticated());
        assertFalse(result.invalid());
        assertEquals("Invalid credentials", result.errorMessage());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void loginReturnsTokensWhenCredentialsAreValid() {
        UUID userId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("alice@example.com");
        when(user.getUsername()).thenReturn("alice");
        when(user.getPasswordHash()).thenReturn("secret1");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(accessTokenService.generateAccessToken(userId)).thenReturn("access-token");

        AuthService.LoginResult result = authService.login("alice@example.com", "secret1", "127.0.0.1", "JUnit");

        assertTrue(result.authenticated());
        assertFalse(result.invalid());
        assertNull(result.errorMessage());
        assertEquals("Bearer", result.tokens().tokenType());
        assertEquals("access-token", result.tokens().accessToken());
        assertNotNull(result.tokens().refreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshReturnsInvalidWhenTokenIsBlank() {
        AuthService.RefreshResult result = authService.refresh("   ");

        assertFalse(result.refreshed());
        assertTrue(result.invalid());
        assertEquals("refreshToken is required", result.errorMessage());
    }

    @Test
    void refreshReturnsUnauthorizedAndRevokesWhenTokenExpired() {
        RefreshToken token = org.mockito.Mockito.mock(RefreshToken.class);

        when(refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(anyString())).thenReturn(Optional.of(token));
        when(token.isExpired(any(OffsetDateTime.class))).thenReturn(true);

        AuthService.RefreshResult result = authService.refresh("refresh-token");

        assertFalse(result.refreshed());
        assertFalse(result.invalid());
        assertEquals("Refresh token expired", result.errorMessage());
        verify(token).revokeNow();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void refreshReturnsNewTokensWhenTokenIsValid() {
        UUID userId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);
        RefreshToken token = org.mockito.Mockito.mock(RefreshToken.class);

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("alice@example.com");
        when(user.getUsername()).thenReturn("alice");

        when(token.isExpired(any(OffsetDateTime.class))).thenReturn(false);
        when(token.getUser()).thenReturn(user);
        when(token.getIpAddress()).thenReturn("127.0.0.1");
        when(token.getUserAgent()).thenReturn("JUnit");

        when(refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(anyString())).thenReturn(Optional.of(token));
        when(accessTokenService.generateAccessToken(userId)).thenReturn("new-access-token");

        AuthService.RefreshResult result = authService.refresh("valid-refresh-token");

        assertTrue(result.refreshed());
        assertFalse(result.invalid());
        assertEquals("Bearer", result.tokens().tokenType());
        assertEquals("new-access-token", result.tokens().accessToken());
        assertNotNull(result.tokens().refreshToken());

        verify(token).revokeNow();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void logoutReturnsUnauthorizedWhenTokenDoesNotExist() {
        when(refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(anyString())).thenReturn(Optional.empty());

        AuthService.LogoutResult result = authService.logout("refresh-token");

        assertFalse(result.loggedOut());
        assertFalse(result.invalid());
        assertEquals("Invalid refresh token", result.errorMessage());
    }

    @Test
    void logoutRevokesTokenWhenTokenExists() {
        RefreshToken token = org.mockito.Mockito.mock(RefreshToken.class);
        when(refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(anyString())).thenReturn(Optional.of(token));

        AuthService.LogoutResult result = authService.logout("refresh-token");

        assertTrue(result.loggedOut());
        assertFalse(result.invalid());
        assertNull(result.errorMessage());
        verify(token).revokeNow();
        verify(refreshTokenRepository).save(token);
    }
}

