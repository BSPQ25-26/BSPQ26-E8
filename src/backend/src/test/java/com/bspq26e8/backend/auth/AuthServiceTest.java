package com.bspq26e8.backend.auth;

import com.bspq26e8.backend.auth.dto.LoginResponse;
import com.bspq26e8.backend.auth.security.JwtProvider;
import com.bspq26e8.backend.user.User;
import com.bspq26e8.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=mySecretKeyThatIsAtLeast32CharactersLongForHS256Algorithm12345678",
    "jwt.expiration=3600000",
    "jwt.refresh.expiration=604800000"
})
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private UUID testUserId;
    private String rawPassword = "securePassword123";

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        String hashedPassword = passwordEncoder.encode(rawPassword);
        testUser = new User("test@example.com", "testuser", hashedPassword);
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, testUserId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testLogin_Success() throws AuthenticationException {
        com.bspq26e8.backend.auth.dto.LoginRequest request = 
            new com.bspq26e8.backend.auth.dto.LoginRequest("test@example.com", rawPassword);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertTrue(response.expiresIn() > 0);
        assertTrue(jwtProvider.validateToken(response.accessToken()));
        assertTrue(jwtProvider.validateToken(response.refreshToken()));
    }

    @Test
    void testLogin_UserNotFound() {
        com.bspq26e8.backend.auth.dto.LoginRequest request = 
            new com.bspq26e8.backend.auth.dto.LoginRequest("nonexistent@example.com", "password");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_WrongPassword() {
        com.bspq26e8.backend.auth.dto.LoginRequest request = 
            new com.bspq26e8.backend.auth.dto.LoginRequest("test@example.com", "wrongPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    void testRefreshAccessToken_Success() throws AuthenticationException {
        String refreshToken = jwtProvider.generateRefreshToken(testUserId);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        LoginResponse response = authService.refreshAccessToken(refreshToken);

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertEquals(refreshToken, response.refreshToken());
        assertTrue(jwtProvider.validateToken(response.accessToken()));
    }

    @Test
    void testRefreshAccessToken_InvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThrows(AuthenticationException.class, () -> authService.refreshAccessToken(invalidToken));
    }

    @Test
    void testRefreshAccessToken_UserNotFound() {
        String refreshToken = jwtProvider.generateRefreshToken(testUserId);

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> authService.refreshAccessToken(refreshToken));
    }

    @Test
    void testLoginGeneratesDifferentTokenTypes() throws AuthenticationException {
        com.bspq26e8.backend.auth.dto.LoginRequest request = 
            new com.bspq26e8.backend.auth.dto.LoginRequest("test@example.com", rawPassword);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginResponse response = authService.login(request);

        assertNotEquals(response.accessToken(), response.refreshToken());
    }
}
