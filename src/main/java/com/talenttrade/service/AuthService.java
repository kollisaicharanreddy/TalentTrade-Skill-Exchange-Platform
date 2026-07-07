package com.talenttrade.service;

import com.talenttrade.dto.LoginRequest;
import com.talenttrade.dto.LoginResponse;
import com.talenttrade.dto.RegisterRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.User;
import com.talenttrade.entity.VerificationToken;
import com.talenttrade.exception.AccountNotVerifiedException;
import com.talenttrade.exception.DuplicateResourceException;
import com.talenttrade.exception.InvalidRequestException;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.exception.VerificationTokenExpiredException;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.repository.VerificationTokenRepository;
import com.talenttrade.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

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
                .emailVerified(false)
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}, email: {}", savedUser.getId(), savedUser.getEmail());

        // Generate verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(savedUser)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        
        verificationTokenRepository.save(verificationToken);

        // Send email
        try {
            String verificationUrl = appUrl + "/api/auth/verify?token=" + token;
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), verificationUrl);
        } catch (Exception e) {
            log.error("Failed to send verification email on registration. User created as unverified.", e);
        }

        return mapToUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting to login user with email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (org.springframework.security.authentication.DisabledException e) {
            log.warn("Login failed - account not verified for email: {}", request.getEmail());
            throw new AccountNotVerifiedException("Account is not verified. Please verify your email.");
        }

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

    @Transactional
    public void verifyToken(String token) {
        log.info("Attempting to verify token: {}", token);
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRequestException("Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Token expired: {}", token);
            throw new VerificationTokenExpiredException("Verification link has expired");
        }

        User user = verificationToken.getUser();
        if (user.isEmailVerified()) {
            log.warn("User already verified: {}", user.getEmail());
            throw new InvalidRequestException("Account is already verified");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        // Delete token to prevent reuse
        verificationTokenRepository.delete(verificationToken);
        log.info("Account verified successfully for user: {}", user.getEmail());
    }

    @Transactional
    public void resendVerificationToken(String email) {
        log.info("Request to resend verification token for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            log.warn("User already verified, no need to resend: {}", email);
            throw new InvalidRequestException("Account is already verified");
        }

        // Delete old token if exists
        verificationTokenRepository.findByUser(user).ifPresent(verificationTokenRepository::delete);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(verificationToken);

        try {
            String verificationUrl = appUrl + "/api/auth/verify?token=" + token;
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationUrl);
        } catch (Exception e) {
            log.error("Failed to resend verification email. User will need to obtain the token manually or try again.", e);
        }
        log.info("Verification email resent successfully to {}", email);
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
