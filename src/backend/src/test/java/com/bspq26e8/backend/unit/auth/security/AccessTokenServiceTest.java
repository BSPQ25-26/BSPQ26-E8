package com.bspq26e8.backend.unit.auth.security;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import com.bspq26e8.backend.auth.security.AccessTokenService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessTokenServiceTest {

    private static final String TOKEN_SECRET = "test-access-token-secret";

    private static final Instant NOW = Instant.parse("2026-05-03T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final AccessTokenService accessTokenService = new AccessTokenService(TOKEN_SECRET, FIXED_CLOCK);

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

    @Test
    void extractUserIdFromTokenReturnsEmptyWhenTokenIsExpired() {
        UUID userId = UUID.randomUUID();
        Clock issuedAtClock = Clock.fixed(NOW.minusSeconds(901), ZoneOffset.UTC);
        AccessTokenService issuingService = new AccessTokenService(TOKEN_SECRET, issuedAtClock);
        String token = issuingService.generateAccessToken(userId);

        Optional<UUID> extracted = accessTokenService.extractUserIdFromToken(token);

        assertTrue(extracted.isEmpty());
    }

    @Test
    void extractUserIdFromTokenAcceptsTokenBeforeExpiration() {
        UUID userId = UUID.randomUUID();
        Clock issuedAtClock = Clock.fixed(NOW.minusSeconds(899), ZoneOffset.UTC);
        AccessTokenService issuingService = new AccessTokenService(TOKEN_SECRET, issuedAtClock);
        String token = issuingService.generateAccessToken(userId);

        Optional<UUID> extracted = accessTokenService.extractUserIdFromToken(token);

        assertTrue(extracted.isPresent());
        assertEquals(userId, extracted.get());
    }
}

