package com.talenttrade.service;

import com.talenttrade.dto.UpdateProfileRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.Role;
import com.talenttrade.entity.User;
import com.talenttrade.exception.DuplicateResourceException;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Test: Get profile for existing user\n" +
                 "Why: Verifies fetching profile returns mapped response DTO.\n" +
                 "Expected: User profile matching email is returned.")
    void getProfile_success() {
        User user = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile("john@example.com");

        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
        assertEquals("johndoe", response.getUsername());
    }

    @Test
    @DisplayName("Test: Get profile throws exception if user not found\n" +
                 "Why: Verifies error code trigger when querying email that does not exist.\n" +
                 "Expected: ResourceNotFoundException is thrown.")
    void getProfile_fail_userNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getProfile("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Test: Successful update of profile\n" +
                 "Why: Verifies that bio, location, and username can be successfully changed if the new username doesn't conflict.\n" +
                 "Expected: User is updated and saved successfully.")
    void updateProfile_success() {
        User user = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("John Doe Updated")
                .username("john_new")
                .bio("New Bio")
                .location("New York")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("john_new")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateProfile("john@example.com", request);

        assertNotNull(response);
        assertEquals("john_new", response.getUsername());
        assertEquals("John Doe Updated", response.getFullName());
        assertEquals("New Bio", response.getBio());
        assertEquals("New York", response.getLocation());
    }

    @Test
    @DisplayName("Test: Update profile fails due to duplicate username\n" +
                 "Why: Ensures username uniqueness checks are enforced during updates.\n" +
                 "Expected: DuplicateResourceException is thrown.")
    void updateProfile_fail_duplicateUsername() {
        User user = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("John Doe Updated")
                .username("existing_user")
                .bio("New Bio")
                .location("New York")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.updateProfile("john@example.com", request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test: Find all users with pagination\n" +
                 "Why: Validates that list user pages load and map correctly.\n" +
                 "Expected: Returned page contains the mapped user responses.")
    void getAllUsers_success() {
        User user = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("john@example.com", result.getContent().get(0).getEmail());
    }
}
