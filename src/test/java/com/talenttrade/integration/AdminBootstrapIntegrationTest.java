package com.talenttrade.integration;

import com.talenttrade.BaseIntegrationTest;
import com.talenttrade.dto.RegisterRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.Role;
import com.talenttrade.entity.User;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class AdminBootstrapIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Workflow 5 Integration: Admin Bootstrap -> ADMIN_EMAIL -> Verify ADMIN Role")
    void adminBootstrapWorkflow() {
        // Clear pre-seeded admin user created by DataInitializer to prevent duplicate email error
        userRepository.findByEmail("saicharanreddykolli@gmail.com").ifPresent(userRepository::delete);

        // Register with admin.email configured in application-test.properties (saicharanreddykolli@gmail.com)
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Charan Admin")
                .email("saicharanreddykolli@gmail.com")
                .username("charanadmin")
                .password("securePassword")
                .build();

        UserResponse response = authService.register(registerRequest);
        assertNotNull(response);
        assertEquals(Role.ADMIN.name(), response.getRole());

        User storedUser = userRepository.findByEmail("saicharanreddykolli@gmail.com").orElseThrow();
        assertEquals(Role.ADMIN, storedUser.getRole());
    }
}
