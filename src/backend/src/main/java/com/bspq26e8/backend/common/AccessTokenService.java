package com.bspq26e8.backend.common;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60;
    private final String tokenSecret;
    private final Clock clock;

    @Autowired
    public AccessTokenService(
            @Value("${security.access-token.secret:bspq26e8-dev-access-token-secret}") String tokenSecret
    ) {
        this(tokenSecret, Clock.systemUTC());
    }

    public AccessTokenService(String tokenSecret, Clock clock) {
        this.tokenSecret = tokenSecret;
        this.clock = clock;
    }

    public String generateAccessToken(UUID userId) {
        String payload = userId + ":" + Instant.now(clock).getEpochSecond();
        String encodedPayload = base64UrlEncode(payload);
        String signature = base64UrlEncode(hmacSha256(payload));
        return encodedPayload + "." + signature;
    }

    public Optional<UUID> extractUserIdFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return extractUserIdFromToken(token);
    }

    public Optional<UUID> extractUserIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return Optional.empty();
        }

        String payload;
        try {
            payload = base64UrlDecode(parts[0]);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        String expectedSignature = base64UrlEncode(hmacSha256(payload));
        if (!constantTimeEquals(expectedSignature, parts[1])) {
            return Optional.empty();
        }

        String[] payloadParts = payload.split(":", -1);
        if (payloadParts.length != 2) {
            return Optional.empty();
        }

        try {
            UUID userId = UUID.fromString(payloadParts[0]);
            Instant issuedAt = Instant.ofEpochSecond(Long.parseLong(payloadParts[1]));
            if (isExpired(issuedAt)) {
                return Optional.empty();
            }
            return Optional.of(userId);
        } catch (DateTimeException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private boolean isExpired(Instant issuedAt) {
        Instant expiresAt = issuedAt.plusSeconds(ACCESS_TOKEN_TTL_SECONDS);
        return !expiresAt.isAfter(Instant.now(clock));
    }

    private byte[] hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Unable to calculate token signature", ex);
        }
    }

    private String base64UrlEncode(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private String base64UrlDecode(String encoded) {
        byte[] bytes = Base64.getUrlDecoder().decode(encoded);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
