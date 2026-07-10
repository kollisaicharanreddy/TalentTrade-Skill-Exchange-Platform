package com.talenttrade.service;

import com.talenttrade.entity.AuthProvider;
import com.talenttrade.entity.Role;
import com.talenttrade.entity.User;
import com.talenttrade.repository.*;
import com.talenttrade.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private UserSkillRepository userSkillRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("Test: Successful dashboard summary statistics gathering\n" +
            "Why: Verifies that admin dashboard correctly aggregates platform stats from all key repositories.\n" +
            "Expected: Returns a Map containing correct count statistics.")
    void getDashboardSummary_success() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByEnabled(true)).thenReturn(80L);
        when(userRepository.countByEmailVerified(true)).thenReturn(70L);
        when(userRepository.countByProvider(AuthProvider.GOOGLE)).thenReturn(40L);
        when(userRepository.countByProvider(AuthProvider.LOCAL)).thenReturn(60L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(5L);
        when(userRepository.countByRole(Role.USER)).thenReturn(95L);
        when(skillRepository.count()).thenReturn(20L);
        when(matchRepository.count()).thenReturn(15L);
        when(exchangeRequestRepository.count()).thenReturn(10L);
        when(sessionRepository.count()).thenReturn(8L);

        Map<String, Object> summary = adminService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(100L, summary.get("totalUsers"));
        assertEquals(80L, summary.get("activeUsers"));
        assertEquals(20L, summary.get("skills"));
        assertEquals(15L, summary.get("matches"));
    }

    @Test
    @DisplayName("Test: Update user status updates enabled flag successfully\n" +
            "Why: Verifies administrator audit-logged ability to enable/disable user accounts.\n" +
            "Expected: Saved user is returned with updated enabled status.")
    void setUserStatus_success() {
        User user = TestDataFactory.createUser(1L, "user@example.com", "user", Role.USER);
        user.setEnabled(true);

        // Mock Security Context for Admin Audit Logger
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@talenttrade.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = adminService.setUserStatus(1L, false);

        assertNotNull(result);
        assertFalse(result.isEnabled());
        verify(userRepository).save(user);

        // Reset Security Context
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Test: Set user role to ADMIN success\n" +
            "Why: Verifies administrator capability to change user roles.\n" +
            "Expected: Saved user is returned with ROLE_ADMIN.")
    void setUserRole_success() {
        User user = TestDataFactory.createUser(1L, "user@example.com", "user", Role.USER);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@talenttrade.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = adminService.setUserRole(1L, "ADMIN");

        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository).save(user);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Test: System Health statistics returned successfully\n" +
            "Why: Verifies that application returns operating metrics and DB check details.\n" +
            "Expected: Returns map containing OS, memory, and database status details.")
    void getSystemHealth_success() throws Exception {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("16.0");

        Map<String, Object> health = adminService.getSystemHealth();

        assertNotNull(health);
        assertEquals("UP", health.get("databaseStatus"));
        assertEquals("PostgreSQL", health.get("databaseProductName"));
        assertTrue(health.containsKey("totalMemoryMb"));
    }
}
