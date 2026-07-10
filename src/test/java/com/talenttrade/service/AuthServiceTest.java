package com.talenttrade.service;

import com.talenttrade.dto.LoginRequest;
import com.talenttrade.dto.LoginResponse;
import com.talenttrade.dto.RegisterRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.AuthProvider;
import com.talenttrade.entity.Role;
import com.talenttrade.entity.User;
import com.talenttrade.exception.DuplicateResourceException;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.security.JwtService;
import com.talenttrade.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private static final String ADMIN_EMAIL = "admin@talenttrade.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "adminEmail", ADMIN_EMAIL);
    }

    @Test
    @DisplayName("Test: Successful registration of a normal user\n" +
                 "Why: Verifies that a user is successfully persisted with the correct local provider, encoded password, and USER role when email doesn't match the admin email.\n" +
                 "Expected: Saved user is returned with role USER and local authentication provider.")
    void register_success_normalUser() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .username("johndoe")
                .password("password123")
                .build();

        User savedUser = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.USER.name(), response.getRole());
        assertEquals(AuthProvider.LOCAL.name(), response.getProvider());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Test: Successful registration of an admin user\n" +
                 "Why: Verifies that a user is registered as an ADMIN when their email matches the bootstrapped ADMIN_EMAIL.\n" +
                 "Expected: Saved user is returned with role ADMIN.")
    void register_success_adminUser() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Admin User")
                .email(ADMIN_EMAIL)
                .username("admin")
                .password("adminpass")
                .build();

        User savedUser = TestDataFactory.createUser(2L, ADMIN_EMAIL, "admin", Role.ADMIN);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedAdminPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(Role.ADMIN.name(), response.getRole());
    }

    @Test
    @DisplayName("Test: Registration fails due to duplicate email\n" +
                 "Why: Ensures the service rejects signup when the email is already in use to maintain data integrity.\n" +
                 "Expected: DuplicateResourceException is thrown.")
    void register_fail_duplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .username("johndoe")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Test: Successful login of an existing user\n" +
                 "Why: Validates credentials checking and proper JWT generation on correct authentication.\n" +
                 "Expected: Returns a LoginResponse containing a valid token and user details.")
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        User user = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mockToken");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("john@example.com", response.getUser().getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Test: Login fails due to non-existent email\n" +
                 "Why: Verifies that incorrect email login is blocked early.\n" +
                 "Expected: BadCredentialsException is thrown.")
    void login_fail_userNotFound() {
        LoginRequest request = LoginRequest.builder()
                .email("unknown@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(authenticationManager, never()).authenticate(any());
    }
}
