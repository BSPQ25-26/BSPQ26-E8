package com.bspq26e8.backend.auth.security;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessTokenServiceTest {

    private final AccessTokenService accessTokenService = new AccessTokenService("test-access-token-secret");

    @Test
    void generateAccessTokenAndExtractUserId() {
        UUID userId = UUID.randomUUID();

        String token = accessTokenService.generateAccessToken(userId);
        Optional<UUID> extracted = accessTokenService.extractUserIdFromToken(token);

        assertTrue(extracted.isPresent());
        assertEquals(userId, extracted.get());
    }

    @Test
    void extractUserIdFromAuthorizationHeaderParsesBearerToken() {
        UUID userId = UUID.randomUUID();
        String token = accessTokenService.generateAccessToken(userId);

        Optional<UUID> extracted = accessTokenService.extractUserIdFromAuthorizationHeader("Bearer   " + token + "   ");

        assertTrue(extracted.isPresent());
        assertEquals(userId, extracted.get());
    }

    @Test
    void extractUserIdFromAuthorizationHeaderReturnsEmptyForInvalidPrefix() {
        UUID userId = UUID.randomUUID();
        String token = accessTokenService.generateAccessToken(userId);

        assertTrue(accessTokenService.extractUserIdFromAuthorizationHeader(null).isEmpty());
        assertTrue(accessTokenService.extractUserIdFromAuthorizationHeader("Basic " + token).isEmpty());
    }

    @Test
    void extractUserIdFromTokenReturnsEmptyForNullBlankOrMalformedToken() {
        assertTrue(accessTokenService.extractUserIdFromToken(null).isEmpty());
        assertTrue(accessTokenService.extractUserIdFromToken("   ").isEmpty());
        assertTrue(accessTokenService.extractUserIdFromToken("onlypayload").isEmpty());
        assertTrue(accessTokenService.extractUserIdFromToken("a.b.c").isEmpty());
    }

    @Test
    void extractUserIdFromTokenReturnsEmptyForInvalidBase64Payload() {
        Optional<UUID> extracted = accessTokenService.extractUserIdFromToken("not-base64.signature");

        assertTrue(extracted.isEmpty());
    }

    @Test
    void extractUserIdFromTokenReturnsEmptyWhenSignatureIsTampered() {
        UUID userId = UUID.randomUUID();
        String token = accessTokenService.generateAccessToken(userId);
        String[] parts = token.split("\\.");
        String tamperedSignature = parts[1] + "x";

        Optional<UUID> extracted = accessTokenService.extractUserIdFromToken(parts[0] + "." + tamperedSignature);

        assertTrue(extracted.isEmpty());
    }

    @Test
    void extractUserIdFromTokenReturnsEmptyWhenPayloadIsTampered() {
        UUID userId = UUID.randomUUID();
        String token = accessTokenService.generateAccessToken(userId);
        String[] parts = token.split("\\.");

        String tamperedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString("not-a-uuid:123".getBytes());
        Optional<UUID> extracted = accessTokenService.extractUserIdFromToken(tamperedPayload + "." + parts[1]);

        assertFalse(extracted.isPresent());
    }
}

