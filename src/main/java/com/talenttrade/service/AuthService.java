package com.talenttrade.service;

import com.talenttrade.dto.LoginRequest;
import com.talenttrade.dto.LoginResponse;
import com.talenttrade.dto.RegisterRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.User;
import com.talenttrade.exception.DuplicateResourceException;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}, email: {}", savedUser.getId(), savedUser.getEmail());

        return mapToUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting to login user with email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found after successful authentication check for email: {}", request.getEmail());
                    return new UsernameNotFoundException("User not found with email: " + request.getEmail());
                });

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
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
