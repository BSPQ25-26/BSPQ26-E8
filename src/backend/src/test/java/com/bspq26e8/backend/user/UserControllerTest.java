package com.bspq26e8.backend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User("test@example.com", "testuser", "hashedPassword");
        // We need to set the ID via reflection since it's generated
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, testUserId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetUserById_Success() throws Exception {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/{id}", testUserId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testUserId.toString()))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserByUsername_Success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/username/{username}", "testuser"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetUserByUsername_NotFound() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/username/{username}", "nonexistent"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser_Success() throws Exception {
        String createRequest = """
            {
              "email": "newuser@example.com",
              "username": "newuser",
              "password": "securePassword123"
            }
            """;

        User newUser = new User("newuser@example.com", "newuser", "hashedPassword");
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(newUser, UUID.randomUUID());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        when(userService.isValidUsername("newuser")).thenReturn(true);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userService.createUser("newuser@example.com", "newuser", "securePassword123")).thenReturn(newUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void testCreateUser_InvalidUsername() throws Exception {
        String createRequest = """
            {
              "email": "test@example.com",
              "username": "ab",
              "password": "password123"
            }
            """;

        when(userService.isValidUsername("ab")).thenReturn(false);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateUser_EmailAlreadyExists() throws Exception {
        String createRequest = """
            {
              "email": "test@example.com",
              "username": "newuser",
              "password": "password123"
            }
            """;

        when(userService.isValidUsername("newuser")).thenReturn(true);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Email already in use"));
    }

    @Test
    void testCreateUser_UsernameAlreadyExists() throws Exception {
        String createRequest = """
            {
              "email": "new@example.com",
              "username": "testuser",
              "password": "password123"
            }
            """;

        when(userService.isValidUsername("testuser")).thenReturn(true);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Username already in use"));
    }
}
