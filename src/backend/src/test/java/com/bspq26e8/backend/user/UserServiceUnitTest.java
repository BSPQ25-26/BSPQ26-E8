package com.bspq26e8.backend.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void testIsValidUsername_Success() {
        assertTrue(userService.isValidUsername("validUser123"));
        assertTrue(userService.isValidUsername("abc"));
        assertTrue(userService.isValidUsername("user_123"));
    }

    @Test
    void testIsValidUsername_TooShort() {
        assertFalse(userService.isValidUsername("ab"));
    }

    @Test
    void testIsValidUsername_TooLong() {
        assertFalse(userService.isValidUsername("a".repeat(31)));
    }

    @Test
    void testIsValidUsername_InvalidCharacters() {
        assertFalse(userService.isValidUsername("user@name"));
        assertFalse(userService.isValidUsername("user-name"));
        assertFalse(userService.isValidUsername("user name"));
    }

    @Test
    void testIsValidUsername_OnlyUnderscore() {
        assertTrue(userService.isValidUsername("_"));
    }

    @Test
    void testIsValidUsername_MixedAlphanumericAndUnderscore() {
        assertTrue(userService.isValidUsername("test_user_123"));
    }

    @Test
    void testIsValidUsername_UppercaseLetters() {
        assertTrue(userService.isValidUsername("TestUser"));
    }
}
