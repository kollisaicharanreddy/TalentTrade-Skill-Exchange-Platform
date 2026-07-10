package com.talenttrade.security;

import com.talenttrade.entity.AuthProvider;
import com.talenttrade.entity.Role;
import com.talenttrade.entity.User;
import com.talenttrade.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private static final String ADMIN_EMAIL = "admin@talenttrade.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(customOAuth2UserService, "adminEmail", ADMIN_EMAIL);
    }

    @Test
    @DisplayName("Test: New OAuth2 user gets registered\n" +
                 "Why: Verifies that a Google user logging in for the first time has an account automatically created with the GOOGLE provider and USER role.\n" +
                 "Expected: User is created, password is set dynamically, and a CustomOAuth2User is returned.")
    void loadUser_newUser_registersUser() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "testuser@gmail.com");
        attributes.put("name", "Test User");

        when(oAuth2User.getAttribute("email")).thenReturn("testuser@gmail.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        when(userRepository.findByEmail("testuser@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedRandomPass");

        User savedUser = User.builder()
                .id(1L)
                .email("testuser@gmail.com")
                .username("testuser")
                .fullName("Test User")
                .role(Role.USER)
                .provider(AuthProvider.GOOGLE)
                .emailVerified(true)
                .enabled(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Invoke the private processOAuth2User method via reflection
        OAuth2User result = ReflectionTestUtils.invokeMethod(
                customOAuth2UserService, "processOAuth2User", oAuth2User);

        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        assertEquals("testuser@gmail.com", result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Test: New OAuth2 Admin User bootstrapping\n" +
                 "Why: Ensures that if a new user's email matches ADMIN_EMAIL, they get ROLE_ADMIN automatically on creation.\n" +
                 "Expected: The created user has Role.ADMIN.")
    void loadUser_newAdminUser_bootstrapsAdmin() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", ADMIN_EMAIL);
        attributes.put("name", "Admin Guy");

        when(oAuth2User.getAttribute("email")).thenReturn(ADMIN_EMAIL);
        when(oAuth2User.getAttribute("name")).thenReturn("Admin Guy");
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedRandomPass");

        User savedUser = User.builder()
                .id(2L)
                .email(ADMIN_EMAIL)
                .username("admin")
                .fullName("Admin Guy")
                .role(Role.ADMIN)
                .provider(AuthProvider.GOOGLE)
                .emailVerified(true)
                .enabled(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Invoke the private processOAuth2User method via reflection
        OAuth2User result = ReflectionTestUtils.invokeMethod(
                customOAuth2UserService, "processOAuth2User", oAuth2User);

        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        assertEquals(Role.ADMIN, ((CustomOAuth2User) result).getUser().getRole());
    }

    @Test
    @DisplayName("Test: Existing user logins via OAuth2\n" +
                 "Why: Verifies that logging in with an existing email updates provider to GOOGLE and ensures verification flag is true.\n" +
                 "Expected: Existing user is updated and saved without registering a duplicate.")
    void loadUser_existingUser_updatesUser() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "existing@gmail.com");
        attributes.put("name", "Existing User");

        when(oAuth2User.getAttribute("email")).thenReturn("existing@gmail.com");
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        User existingUser = User.builder()
                .id(3L)
                .email("existing@gmail.com")
                .username("existing")
                .fullName("Existing User")
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .emailVerified(false)
                .enabled(false)
                .build();
        when(userRepository.findByEmail("existing@gmail.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Invoke the private processOAuth2User method via reflection
        OAuth2User result = ReflectionTestUtils.invokeMethod(
                customOAuth2UserService, "processOAuth2User", oAuth2User);

        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        User innerUser = ((CustomOAuth2User) result).getUser();
        assertTrue(innerUser.isEmailVerified());
        assertTrue(innerUser.isEnabled());
        assertEquals(AuthProvider.GOOGLE, innerUser.getProvider());
        verify(userRepository).save(existingUser);
    }
}
