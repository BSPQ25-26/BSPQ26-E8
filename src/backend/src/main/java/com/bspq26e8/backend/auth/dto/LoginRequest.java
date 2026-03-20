package com.bspq26e8.backend.auth.dto;

public record LoginRequest(
    String email,
    String password
) {
}
