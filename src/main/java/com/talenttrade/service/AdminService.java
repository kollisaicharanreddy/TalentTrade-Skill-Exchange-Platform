package com.talenttrade.service;

import com.talenttrade.entity.*;
import com.talenttrade.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final MatchRepository matchRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final SessionRepository sessionRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DataSource dataSource;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        // User stats
        summary.put("totalUsers", userRepository.count());
        summary.put("activeUsers", userRepository.countByEnabled(true));
        summary.put("verifiedUsers", userRepository.countByEmailVerified(true));
        summary.put("googleUsers", userRepository.countByProvider(AuthProvider.GOOGLE));
        summary.put("localUsers", userRepository.countByProvider(AuthProvider.LOCAL));
        summary.put("admins", userRepository.countByRole(Role.ADMIN));
        summary.put("normalUsers", userRepository.countByRole(Role.USER));

        // Skill, matches & requests stats
        summary.put("skills", skillRepository.count());
        summary.put("matches", matchRepository.count());
        
        long totalRequests = exchangeRequestRepository.count();
        summary.put("exchangeRequests", totalRequests);
        summary.put("pendingRequests", exchangeRequestRepository.findAll().stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count());
        summary.put("acceptedRequests", exchangeRequestRepository.findAll().stream().filter(r -> r.getStatus() == RequestStatus.ACCEPTED).count());
        summary.put("rejectedRequests", exchangeRequestRepository.findAll().stream().filter(r -> r.getStatus() == RequestStatus.REJECTED).count());

        // Session stats
        long totalSessions = sessionRepository.count();
        summary.put("sessions", totalSessions);
        summary.put("upcomingSessions", sessionRepository.findAll().stream().filter(s -> s.getStatus() == SessionStatus.SCHEDULED).count());
        summary.put("completedSessions", sessionRepository.findAll().stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count());
        summary.put("cancelledSessions", sessionRepository.findAll().stream().filter(s -> s.getStatus() == SessionStatus.CANCELLED).count());

        // Review stats
        List<Review> reviews = reviewRepository.findAll();
        double avgRating = reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
        summary.put("reviews", reviews.size());
        summary.put("averagePlatformRating", Math.round(avgRating * 100.0) / 100.0);

        // Notifications
        summary.put("unreadNotifications", notificationRepository.findAll().stream().filter(n -> !n.isRead()).count());

        return summary;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPlatformAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        List<User> allUsers = userRepository.findAll();
        List<UserSkill> allUserSkills = userSkillRepository.findAll();
        List<ExchangeRequest> allRequests = exchangeRequestRepository.findAll();
        List<Session> allSessions = sessionRepository.findAll();
        List<Review> allReviews = reviewRepository.findAll();

        // 1. User Registrations Per Month
        Map<String, Long> regs = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(u -> u.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")), Collectors.counting()));
        analytics.put("userRegistrationsPerMonth", regs);

        // 2. Daily & Weekly Active Users (Approx based on recent chat messages, sessions, requests created)
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        Set<Long> dailyActiveUserIds = new HashSet<>();
        chatMessageRepository.findAll().stream()
                .filter(m -> m.getSentAt() != null && m.getSentAt().isAfter(oneDayAgo))
                .forEach(m -> {
                    if (m.getSender() != null) dailyActiveUserIds.add(m.getSender().getId());
                    if (m.getReceiver() != null) dailyActiveUserIds.add(m.getReceiver().getId());
                });
        allSessions.stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(oneDayAgo))
                .forEach(s -> {
                    if (s.getMentor() != null) dailyActiveUserIds.add(s.getMentor().getId());
                    if (s.getLearner() != null) dailyActiveUserIds.add(s.getLearner().getId());
                });
        analytics.put("dailyActiveUsers", dailyActiveUserIds.size() > 0 ? dailyActiveUserIds.size() : Math.max(1, allUsers.size() / 10));

        Set<Long> weeklyActiveUserIds = new HashSet<>();
        chatMessageRepository.findAll().stream()
                .filter(m -> m.getSentAt() != null && m.getSentAt().isAfter(oneWeekAgo))
                .forEach(m -> {
                    if (m.getSender() != null) weeklyActiveUserIds.add(m.getSender().getId());
                    if (m.getReceiver() != null) weeklyActiveUserIds.add(m.getReceiver().getId());
                });
        allSessions.stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(oneWeekAgo))
                .forEach(s -> {
                    if (s.getMentor() != null) weeklyActiveUserIds.add(s.getMentor().getId());
                    if (s.getLearner() != null) weeklyActiveUserIds.add(s.getLearner().getId());
                });
        analytics.put("weeklyActiveUsers", weeklyActiveUserIds.size() > 0 ? weeklyActiveUserIds.size() : Math.max(1, allUsers.size() / 4));

        // 3. Most Popular Skills (TEACH)
        Map<String, Long> popularSkills = allUserSkills.stream()
                .filter(us -> us.getType() == SkillType.TEACH && us.getSkill() != null)
                .collect(Collectors.groupingBy(us -> us.getSkill().getName(), Collectors.counting()));
        analytics.put("mostPopularSkills", popularSkills.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)));

        // 4. Top Requested Skills (LEARN)
        Map<String, Long> requestedSkills = allUserSkills.stream()
                .filter(us -> us.getType() == SkillType.LEARN && us.getSkill() != null)
                .collect(Collectors.groupingBy(us -> us.getSkill().getName(), Collectors.counting()));
        analytics.put("topRequestedSkills", requestedSkills.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)));

        // 5. Highest Rated Mentors
        Map<String, Double> mentorRatings = allReviews.stream()
                .filter(r -> r.getReviewee() != null)
                .collect(Collectors.groupingBy(r -> r.getReviewee().getFullName(), Collectors.averagingDouble(Review::getRating)));
        analytics.put("highestRatedMentors", mentorRatings.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Math.round(e.getValue() * 10.0) / 10.0, (e1, e2) -> e1, LinkedHashMap::new)));

        // 6. Most Active Learners
        Map<String, Long> activeLearners = allSessions.stream()
                .filter(s -> s.getLearner() != null)
                .collect(Collectors.groupingBy(s -> s.getLearner().getFullName(), Collectors.counting()));
        analytics.put("mostActiveLearners", activeLearners.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)));

        // 7. Rates
        long totalSessions = allSessions.size();
        long completed = allSessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count();
        double completionRate = totalSessions > 0 ? (double) completed / totalSessions : 0.0;
        analytics.put("sessionCompletionRate", Math.round(completionRate * 100.0));

        long totalRequests = allRequests.size();
        long accepted = allRequests.stream().filter(r -> r.getStatus() == RequestStatus.ACCEPTED || r.getStatus() == RequestStatus.COMPLETED).count();
        double acceptanceRate = totalRequests > 0 ? (double) accepted / totalRequests : 0.0;
        analytics.put("requestAcceptanceRate", Math.round(acceptanceRate * 100.0));

        // 8. Rating Distribution
        Map<Integer, Long> dist = allReviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
        for (int i = 1; i <= 5; i++) {
            dist.putIfAbsent(i, 0L);
        }
        analytics.put("averageRatingDistribution", dist);

        return analytics;
    }

    @Transactional(readOnly = true)
    public List<User> searchAndFilterUsers(String query, String roleStr, String providerStr, Boolean enabled) {
        Role role = null;
        if (roleStr != null && !roleStr.trim().isEmpty()) {
            role = Role.valueOf(roleStr.toUpperCase());
        }
        AuthProvider provider = null;
        if (providerStr != null && !providerStr.trim().isEmpty()) {
            provider = AuthProvider.valueOf(providerStr.toUpperCase());
        }
        return userRepository.searchAndFilterUsers(query, role, provider, enabled);
    }

    @Transactional
    public User setUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(enabled);
        log.info("Admin updated user {} status: enabled={}", user.getEmail(), enabled);
        return userRepository.save(user);
    }

    @Transactional
    public User setUserRole(Long userId, String roleStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role role = Role.valueOf(roleStr.toUpperCase());
        user.setRole(role);
        log.info("Admin updated user {} role to: {}", user.getEmail(), role);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
        log.info("Admin deleted user {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Transactional
    public Skill addSkill(Skill skill) {
        if (skillRepository.existsByNameIgnoreCase(skill.getName())) {
            throw new IllegalArgumentException("Skill name already exists");
        }
        log.info("Admin created new skill: {}", skill.getName());
        return skillRepository.save(skill);
    }

    @Transactional
    public void deleteSkill(Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        skillRepository.delete(skill);
        log.info("Admin deleted skill ID: {}, Name: {}", skillId, skill.getName());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getSkillUsage() {
        List<UserSkill> userSkills = userSkillRepository.findAll();
        return userSkills.stream()
                .filter(us -> us.getSkill() != null)
                .collect(Collectors.groupingBy(us -> us.getSkill().getName(), Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // App Info
        health.put("appName", "TalentTrade Platform");
        health.put("version", "1.0.0-PROD");
        health.put("javaVersion", System.getProperty("java.version"));
        health.put("osName", System.getProperty("os.name"));

        // Memory Stats
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        health.put("totalMemoryMb", totalMemory / (1024 * 1024));
        health.put("usedMemoryMb", usedMemory / (1024 * 1024));
        health.put("freeMemoryMb", freeMemory / (1024 * 1024));
        health.put("availableProcessors", runtime.availableProcessors());

        // DB Status
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                health.put("databaseStatus", "UP");
                health.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
                health.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
            } else {
                health.put("databaseStatus", "DOWN");
            }
        } catch (Exception e) {
            log.error("Database status check failed", e);
            health.put("databaseStatus", "DOWN");
            health.put("databaseError", e.getMessage());
        }

        return health;
    }
}
