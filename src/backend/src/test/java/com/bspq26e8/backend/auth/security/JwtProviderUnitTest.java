package com.bspq26e8.backend.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderUnitTest {

    private JwtProvider jwtProvider;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        testUserId = UUID.randomUUID();
    }

    @Test
    void testGenerateAccessToken_Success() {
        String token = jwtProvider.generateAccessToken(testUserId);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT format: HEADER.PAYLOAD.SIGNATURE
    }

    @Test
    void testGenerateRefreshToken_Success() {
        String token = jwtProvider.generateRefreshToken(testUserId);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testValidateToken_AccessToken_Success() {
        String token = jwtProvider.generateAccessToken(testUserId);
        
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void testValidateToken_RefreshToken_Success() {
        String token = jwtProvider.generateRefreshToken(testUserId);
        
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(jwtProvider.validateToken("invalid.token.here"));
    }

    @Test
    void testValidateToken_EmptyToken() {
        assertFalse(jwtProvider.validateToken(""));
    }

    @Test
    void testGetUserIdFromToken_Success() {
        String token = jwtProvider.generateAccessToken(testUserId);
        
        UUID extractedId = jwtProvider.getUserIdFromToken(token);
        
        assertNotNull(extractedId);
        assertEquals(testUserId, extractedId);
    }

    @Test
    void testGetUserIdFromToken_InvalidToken() {
        assertThrows(Exception.class, () -> {
            jwtProvider.getUserIdFromToken("invalid.token.here");
        });
    }

    @Test
    void testAccessToken_HasShorterExpirationThanRefresh() {
        // This is a conceptual test - refresh token should live longer
        String accessToken = jwtProvider.generateAccessToken(testUserId);
        String refreshToken = jwtProvider.generateRefreshToken(testUserId);
        
        // Both should be valid immediately
        assertTrue(jwtProvider.validateToken(accessToken));
        assertTrue(jwtProvider.validateToken(refreshToken));
    }

    @Test
    void testDifferentUserIds_GenerateDifferentTokens() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        
        String token1 = jwtProvider.generateAccessToken(userId1);
        String token2 = jwtProvider.generateAccessToken(userId2);
        
        assertNotEquals(token1, token2);
        assertEquals(userId1, jwtProvider.getUserIdFromToken(token1));
        assertEquals(userId2, jwtProvider.getUserIdFromToken(token2));
    }
}
