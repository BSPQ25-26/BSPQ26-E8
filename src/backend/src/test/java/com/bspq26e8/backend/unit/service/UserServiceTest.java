package com.bspq26e8.backend.unit.service;

import com.bspq26e8.backend.user.entity.User;
import com.bspq26e8.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.bspq26e8.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void isValidUsernameChecksFormatAndLength() {
        assertFalse(userService.isValidUsername(null));
        assertFalse(userService.isValidUsername("ab"));
        assertFalse(userService.isValidUsername("a".repeat(31)));
        assertFalse(userService.isValidUsername("invalid-name"));
        assertTrue(userService.isValidUsername("  valid_name_123  "));
    }

    @Test
    void createUserReturnsConflictWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        UserService.CreateUserResult result = userService.createUser(" USER@example.com ", "username", " hash ");

        assertFalse(result.created());
        assertNull(result.user());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserReturnsConflictWhenUsernameAlreadyExists() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("username")).thenReturn(true);

        UserService.CreateUserResult  result = userService.createUser("user@example.com", "username", "hash");

        assertFalse(result.created());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void findByIdReturnsMappedViewWhenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getUsername()).thenReturn("username");
        when(user.isActive()).thenReturn(true);
        when(user.isEmailVerified()).thenReturn(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<UserService.UserView> result = userService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().id());
        assertEquals("user@example.com", result.get().email());
        assertEquals("username", result.get().username());
        assertTrue(result.get().isActive());
        assertTrue(result.get().emailVerified());
    }

    @Test
    void findByEmailNormalizesInputAndMapsView() {
        User user = org.mockito.Mockito.mock(User.class);
        UUID userId = UUID.randomUUID();

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getUsername()).thenReturn("username");
        when(user.isActive()).thenReturn(true);
        when(user.isEmailVerified()).thenReturn(false);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        Optional<UserService.UserView> result = userService.findByEmail(" USER@EXAMPLE.COM ");

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().id());
        assertEquals("user@example.com", result.get().email());
        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void findByEmailReturnsEmptyWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        Optional<UserService.UserView> result = userService.findByEmail("missing@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void updateProfileReturnsForbiddenWhenUserDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        UserService.UpdateProfileResult result = userService.updateProfile(userId, authenticatedUserId, "avatar", List.of(1L));

        assertTrue(result.isForbidden());
        assertNull(result.user());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfileReturnsNotFoundWhenUserIsMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserService.UpdateProfileResult result = userService.updateProfile(userId, userId, "avatar", List.of(1L));

        assertTrue(result.isNotFound());
        assertNull(result.user());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateProfileUpdatesProfilePictureAndPreferredLanguages() {
        UUID userId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getUsername()).thenReturn("username");
        when(user.isActive()).thenReturn(true);
        when(user.isEmailVerified()).thenReturn(true);
        when(user.getProfilePicture()).thenReturn("https://img.example/new.png");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findPreferredLanguagesByUserId(userId)).thenReturn(List.of());

        UserService.UpdateProfileResult result = userService.updateProfile(
                userId,
                userId,
                "https://img.example/new.png",
                List.of(1L, 2L)
        );

        assertTrue(result.isUpdated());
        assertNotNull(result.user());
        assertEquals("https://img.example/new.png", result.user().profilePicture());

        ArgumentCaptor<Set<Long>> languagesCaptor = ArgumentCaptor.forClass(Set.class);
        verify(user).setProfilePicture("https://img.example/new.png");
        verify(user).setPreferredLanguageIds(languagesCaptor.capture());
        assertEquals(Set.of(1L, 2L), languagesCaptor.getValue());
        verify(userRepository).save(user);
    }
}
