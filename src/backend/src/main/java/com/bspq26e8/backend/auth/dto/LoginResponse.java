package com.bspq26e8.backend.auth.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {
}
