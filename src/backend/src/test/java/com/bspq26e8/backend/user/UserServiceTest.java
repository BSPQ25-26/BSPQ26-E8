package com.bspq26e8.backend.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bspq26e8.backend.user.service.UserService;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private final UserService userService = new UserService(null);

    @Test
    void shouldAcceptValidUsername() {
        assertTrue(userService.isValidUsername("valid_user123"));
    }

    @Test
    void shouldRejectInvalidUsernames() {
        assertFalse(userService.isValidUsername(null));
        assertFalse(userService.isValidUsername("ab"));
        assertFalse(userService.isValidUsername("has space"));
        assertFalse(userService.isValidUsername("contains-unsupported-char"));
    }
}
