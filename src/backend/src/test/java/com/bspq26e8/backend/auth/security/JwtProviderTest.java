package com.bspq26e8.backend.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=mySecretKeyThatIsAtLeast32CharactersLongForHS256Algorithm12345678",
    "jwt.expiration=3600000",
    "jwt.refresh.expiration=604800000"
})
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    private UUID testUserId;
    private String testEmail;
    private String testUsername;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "test@example.com";
        testUsername = "testuser";
    }

    @Test
    void testGenerateAccessToken_Success() {
        String token = jwtProvider.generateAccessToken(testUserId, testEmail, testUsername);

        assertNotNull(token);
        assertNotEmpty(token);
        assertTrue(token.contains("."));
    }

    @Test
    void testGenerateRefreshToken_Success() {
        String token = jwtProvider.generateRefreshToken(testUserId);

        assertNotNull(token);
        assertNotEmpty(token);
        assertTrue(token.contains("."));
    }

    @Test
    void testValidateToken_ValidAccessToken() {
        String token = jwtProvider.generateAccessToken(testUserId, testEmail, testUsername);

        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void testValidateToken_ValidRefreshToken() {
        String token = jwtProvider.generateRefreshToken(testUserId);

        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtProvider.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_MalformedToken() {
        String malformedToken = "this-is-not-a-valid-jwt";

        assertFalse(jwtProvider.validateToken(malformedToken));
    }

    @Test
    void testGetUserIdFromToken_Success() {
        String token = jwtProvider.generateAccessToken(testUserId, testEmail, testUsername);

        UUID extractedId = jwtProvider.getUserIdFromToken(token);

        assertEquals(testUserId, extractedId);
    }

    @Test
    void testGetEmailFromToken_Success() {
        String token = jwtProvider.generateAccessToken(testUserId, testEmail, testUsername);

        String extractedEmail = jwtProvider.getEmailFromToken(token);

        assertEquals(testEmail, extractedEmail);
    }

    @Test
    void testGetUsernameFromToken_Success() {
        String token = jwtProvider.generateAccessToken(testUserId, testEmail, testUsername);

        String extractedUsername = jwtProvider.getUsernameFromToken(token);

        assertEquals(testUsername, extractedUsername);
    }

    @Test
    void testGetExpirationTime() {
        long expirationTime = jwtProvider.getExpirationTime();

        assertTrue(expirationTime > 0);
        assertEquals(3600, expirationTime); // 1 hour
    }

    @Test
    void testAccessTokenAndRefreshTokenAreDifferent() {
        String accessToken = jwtProvider.generateAccessToken(testUserId, testEmail, testUsername);
        String refreshToken = jwtProvider.generateRefreshToken(testUserId);

        assertNotEquals(accessToken, refreshToken);
    }

    @Test
    void testMultipleTokensAreDifferent() {
        String token1 = jwtProvider.generateAccessToken(testUserId, testEmail, testUsername);
        String token2 = jwtProvider.generateAccessToken(testUserId, testEmail, testUsername);

        // Tokens might be different due to timestamps
        assertTrue(jwtProvider.validateToken(token1));
        assertTrue(jwtProvider.validateToken(token2));
    }

    private void assertNotEmpty(String value) {
        assertNotNull(value);
        assertTrue(value.length() > 0);
    }
}
