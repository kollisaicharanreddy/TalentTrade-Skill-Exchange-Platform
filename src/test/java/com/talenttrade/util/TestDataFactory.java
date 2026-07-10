package com.talenttrade.util;

import com.talenttrade.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TestDataFactory {

    public static User createUser(Long id, String email, String username, Role role) {
        return User.builder()
                .id(id)
                .email(email)
                .username(username)
                .fullName("Test User " + id)
                .password("password123")
                .role(role)
                .provider(AuthProvider.LOCAL)
                .emailVerified(true)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Skill createSkill(Long id, String name, String category) {
        return Skill.builder()
                .id(id)
                .name(name)
                .category(category)
                .description("Test Description for " + name)
                .build();
    }

    public static UserSkill createUserSkill(Long id, User user, Skill skill, SkillType type, SkillLevel level) {
        return UserSkill.builder()
                .id(id)
                .user(user)
                .skill(skill)
                .type(type)
                .level(level)
                .build();
    }

    public static ExchangeRequest createExchangeRequest(Long id, User sender, User receiver, RequestStatus status) {
        return ExchangeRequest.builder()
                .id(id)
                .sender(sender)
                .receiver(receiver)
                .message("Let's trade skills!")
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Session createSession(Long id, ExchangeRequest request, User mentor, User learner, SessionStatus status) {
        return Session.builder()
                .id(id)
                .exchangeRequest(request)
                .mentor(mentor)
                .learner(learner)
                .scheduledDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .meetingLink("https://meet.google.com/abc-defg-hij")
                .status(status)
                .notes("Testing session notes")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
