package com.talenttrade.integration;

import com.talenttrade.BaseIntegrationTest;
import com.talenttrade.dto.LoginRequest;
import com.talenttrade.dto.LoginResponse;
import com.talenttrade.dto.RegisterRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class UserWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Workflow 1 Integration: Register User -> Login -> JWT Generated")
    void registerLoginWorkflow() {
        // 1. Register User
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Integration Tester")
                .email("integration@example.com")
                .username("inttester")
                .password("password123")
                .build();

        UserResponse regResponse = authService.register(registerRequest);
        assertNotNull(regResponse);
        assertEquals("integration@example.com", regResponse.getEmail());
        assertTrue(userRepository.existsByEmail("integration@example.com"));

        // 2. Login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("integration@example.com")
                .password("password123")
                .build();

        LoginResponse loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals("integration@example.com", loginResponse.getUser().getEmail());
    }
}
