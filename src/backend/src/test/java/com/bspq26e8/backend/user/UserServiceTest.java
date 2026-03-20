package com.bspq26e8.backend.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void testIsValidUsername_Valid() {
        assertTrue(userService.isValidUsername("valid_user123"));
        assertTrue(userService.isValidUsername("validuser123"));
        assertTrue(userService.isValidUsername("user_name"));
        assertTrue(userService.isValidUsername("JohnDoe123"));
    }

    @Test
    void testIsValidUsername_TooShort() {
        assertFalse(userService.isValidUsername("ab"));
        assertFalse(userService.isValidUsername("a"));
    }

    @Test
    void testIsValidUsername_TooLong() {
        assertFalse(userService.isValidUsername("a".repeat(31)));
    }

    @Test
    void testIsValidUsername_InvalidCharacters() {
        assertFalse(userService.isValidUsername("user-name")); // hyphen not allowed
        assertFalse(userService.isValidUsername("user.name")); // dot not allowed
        assertFalse(userService.isValidUsername("user@name")); // special char
        assertFalse(userService.isValidUsername("has space")); // space
    }

    @Test
    void testIsValidUsername_Null() {
        assertFalse(userService.isValidUsername(null));
    }

    @Test
    void testIsValidUsername_Empty() {
        assertFalse(userService.isValidUsername(""));
        assertFalse(userService.isValidUsername("   "));
    }

    @Test
    void testCreateUser_Success() {
        String plainPassword = "plainPassword123";
        String hashedPassword = "$2a$10$hashedPassword";
        
        when(passwordEncoder.encode(plainPassword)).thenReturn(hashedPassword);
        
        User testUser = new User("test@example.com", "testuser", hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser("test@example.com", "testuser", plainPassword);

        assertNotNull(createdUser);
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("testuser", createdUser.getUsername());
        assertEquals(hashedPassword, createdUser.getPasswordHash());
    }
}
