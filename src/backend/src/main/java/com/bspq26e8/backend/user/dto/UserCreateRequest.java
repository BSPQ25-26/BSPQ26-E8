package com.bspq26e8.backend.user.dto;

public record UserCreateRequest(
    String email,
    String username,
    String password
) {
}
