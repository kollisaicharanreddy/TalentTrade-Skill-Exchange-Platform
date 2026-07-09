package com.talenttrade.service;

import com.talenttrade.dto.LoginRequest;
import com.talenttrade.dto.LoginResponse;
import com.talenttrade.dto.RegisterRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.User;
import com.talenttrade.entity.Role;
import com.talenttrade.entity.AuthProvider;
import com.talenttrade.exception.DuplicateResourceException;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${admin.email:charan@gmail.com}")
    private String adminEmail;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {} and username: {}", request.getEmail(), request.getUsername());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists");
        }

        Role assignedRole = (adminEmail != null && adminEmail.equalsIgnoreCase(request.getEmail())) ? Role.ADMIN : Role.USER;

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(true) // Auto-verified: per day 1 requirements local login continues without verification
                .enabled(true)      // Auto-enabled: bypass verification constraints
                .role(assignedRole)
                .provider(AuthProvider.LOCAL)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}, email: {}, role: {}", savedUser.getId(), savedUser.getEmail(), assignedRole);

        return mapToUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting to login user with email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid email or password"));

        // If email matches ADMIN_EMAIL, automatically promote them to ADMIN
        boolean updated = false;
        if (adminEmail != null && adminEmail.equalsIgnoreCase(user.getEmail()) && user.getRole() != Role.ADMIN) {
            user.setRole(Role.ADMIN);
            updated = true;
        }

        // If registered using Google OAuth, allow login but update provider to LOCAL or link it.
        // Wait, the requirement says "Support LOCAL Authentication, GOOGLE Authentication. Both must coexist."
        // "If email exists: Login existing account. If email does not exist: Create new account. Generate JWT. Redirect."
        // So we can permit coexisting login under the same email.
        if (user.getProvider() == null) {
            user.setProvider(AuthProvider.LOCAL);
            updated = true;
        }

        if (updated) {
            user = userRepository.save(user);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (org.springframework.security.authentication.DisabledException e) {
            // Local users are bypass-verified so they should be enabled. If they are disabled, we might have deactivated them.
            log.warn("Login failed - account disabled for email: {}", request.getEmail());
            throw e;
        }

        String jwtToken = jwtService.generateToken(user);
        log.info("Login successful for user with email: {}", request.getEmail());

        return LoginResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsernameValue())
                .email(user.getEmail())
                .bio(user.getBio())
                .location(user.getLocation())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .emailVerified(user.isEmailVerified())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
