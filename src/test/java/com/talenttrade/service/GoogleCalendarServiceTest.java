package com.talenttrade.service;

import com.talenttrade.entity.*;
import com.talenttrade.repository.UserGoogleCredentialRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleCalendarServiceTest {

    @Mock
    private UserGoogleCredentialRepository credentialRepository;

    @InjectMocks
    private GoogleCalendarService googleCalendarService;

    @Test
    @DisplayName("Test: Silently bypass Google Calendar integration when organizer has no credentials connected")
    void createCalendarEvent_noCredentials_bypass() {
        User mentor = User.builder().id(1L).email("mentor@example.com").fullName("Mentor Test").build();
        User learner = User.builder().id(2L).email("learner@example.com").fullName("Learner Test").build();
        Session session = Session.builder()
                .id(1L)
                .mentor(mentor)
                .learner(learner)
                .scheduledDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .meetingLink("https://meet.jit.si/fallback")
                .build();

        when(credentialRepository.findByUserEmail("mentor@example.com")).thenReturn(Optional.empty());

        Session result = googleCalendarService.createCalendarEvent(session);

        assertEquals(session, result);
        assertNull(result.getGoogleEventId());
        assertNull(result.getCalendarProvider());
        verify(credentialRepository, times(1)).findByUserEmail("mentor@example.com");
    }
}
