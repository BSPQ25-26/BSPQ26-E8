package com.bspq26e8.backend.auth;

import com.bspq26e8.backend.auth.dto.LoginRequest;
import com.bspq26e8.backend.auth.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Mockito injection handled by @ExtendWith
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        LoginResponse response = new LoginResponse(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken123",
            3600
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("invalid@example.com", "wrongPassword");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new AuthenticationException("Invalid email or password"));

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void testLogin_MissingEmail() throws Exception {
        String requestBody = """
            {
              "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_MissingPassword() throws Exception {
        String requestBody = """
            {
              "email": "test@example.com"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_EmptyEmail() throws Exception {
        String requestBody = """
            {
              "email": "",
              "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Email is required"));
    }

    @Test
    void testLogin_EmptyPassword() throws Exception {
        String requestBody = """
            {
              "email": "test@example.com",
              "password": ""
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Password is required"));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        LoginResponse response = new LoginResponse(
            "newAccessToken123",
            "refreshToken123",
            3600
        );

        when(authService.refreshAccessToken("refreshToken123")).thenReturn(response);

        String requestBody = """
            {
              "refreshToken": "refreshToken123"
            }
            """;

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("newAccessToken123"))
            .andExpect(jsonPath("$.refreshToken").value("refreshToken123"))
            .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void testRefreshToken_InvalidToken() throws Exception {
        when(authService.refreshAccessToken("invalidToken"))
            .thenThrow(new AuthenticationException("Invalid refresh token"));

        String requestBody = """
            {
              "refreshToken": "invalidToken"
            }
            """;

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid refresh token"));
    }

    @Test
    void testRefreshToken_MissingRefreshToken() throws Exception {
        String requestBody = "{}";

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Refresh token is required"));
    }

    @Test
    void testRefreshToken_EmptyRefreshToken() throws Exception {
        String requestBody = """
            {
              "refreshToken": ""
            }
            """;

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Refresh token is required"));
    }
}
