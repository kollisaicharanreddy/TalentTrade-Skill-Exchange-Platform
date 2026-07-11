package com.talenttrade.service;

import com.talenttrade.dto.SessionRequestDTO;
import com.talenttrade.dto.SessionResponseDTO;
import com.talenttrade.entity.*;
import com.talenttrade.exception.InvalidSessionStateException;
import com.talenttrade.repository.ExchangeRequestRepository;
import com.talenttrade.repository.SessionRepository;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CalendarService calendarService;

    @InjectMocks
    private SessionService sessionService;

    @Test
    @DisplayName("Test: Successfully schedule a session\n" +
            "Why: Verifies that session can be created for an accepted request when no conflicts exist.\n" +
            "Expected: Returns a scheduled SessionResponseDTO and triggers notification to both participants.")
    void createSession_success() {
        User mentor = TestDataFactory.createUser(1L, "mentor@example.com", "mentor", Role.USER);
        User learner = TestDataFactory.createUser(2L, "learner@example.com", "learner", Role.USER);
        ExchangeRequest request = TestDataFactory.createExchangeRequest(1L, mentor, learner, RequestStatus.ACCEPTED);

        SessionRequestDTO dto = SessionRequestDTO.builder()
                .exchangeRequestId(1L)
                .mentorId(1L)
                .learnerId(2L)
                .scheduledDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .meetingLink("https://meet.google.com/xyz")
                .notes("Learning Java")
                .build();

        Session savedSession = TestDataFactory.createSession(1L, request, mentor, learner, SessionStatus.SCHEDULED);

        when(exchangeRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(sessionRepository.existsByExchangeRequestId(1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(userRepository.findById(2L)).thenReturn(Optional.of(learner));

        when(sessionRepository.hasTimeConflict(1L, dto.getScheduledDate(), dto.getStartTime(), dto.getEndTime()))
                .thenReturn(false);
        when(sessionRepository.hasTimeConflict(2L, dto.getScheduledDate(), dto.getStartTime(), dto.getEndTime()))
                .thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);
        when(calendarService.createCalendarEvent(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionResponseDTO response = sessionService.createSession("mentor@example.com", dto);

        assertNotNull(response);
        assertEquals(SessionStatus.SCHEDULED, response.getStatus());
        verify(notificationService, times(2)).createNotification(any(), any(), any(),
                eq(NotificationType.SESSION_CREATED));
    }

    @Test
    @DisplayName("Test: Schedule session fails if request is not accepted\n" +
            "Why: Enforces request workflow constraint (must be accepted before scheduling).\n" +
            "Expected: InvalidSessionStateException is thrown.")
    void createSession_fail_requestNotAccepted() {
        User mentor = TestDataFactory.createUser(1L, "mentor@example.com", "mentor", Role.USER);
        User learner = TestDataFactory.createUser(2L, "learner@example.com", "learner", Role.USER);
        ExchangeRequest request = TestDataFactory.createExchangeRequest(1L, mentor, learner, RequestStatus.PENDING);

        SessionRequestDTO dto = SessionRequestDTO.builder()
                .exchangeRequestId(1L)
                .mentorId(1L)
                .learnerId(2L)
                .scheduledDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .build();

        when(exchangeRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(InvalidSessionStateException.class, () -> sessionService.createSession("mentor@example.com", dto));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test: Schedule session fails due to mentor time conflict\n" +
            "Why: Verifies validation check for double-booking of mentors.\n" +
            "Expected: InvalidSessionStateException is thrown.")
    void createSession_fail_mentorTimeConflict() {
        User mentor = TestDataFactory.createUser(1L, "mentor@example.com", "mentor", Role.USER);
        User learner = TestDataFactory.createUser(2L, "learner@example.com", "learner", Role.USER);
        ExchangeRequest request = TestDataFactory.createExchangeRequest(1L, mentor, learner, RequestStatus.ACCEPTED);

        SessionRequestDTO dto = SessionRequestDTO.builder()
                .exchangeRequestId(1L)
                .mentorId(1L)
                .learnerId(2L)
                .scheduledDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .build();

        when(exchangeRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(sessionRepository.existsByExchangeRequestId(1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(userRepository.findById(2L)).thenReturn(Optional.of(learner));

        when(sessionRepository.hasTimeConflict(1L, dto.getScheduledDate(), dto.getStartTime(), dto.getEndTime()))
                .thenReturn(true);

        assertThrows(InvalidSessionStateException.class, () -> sessionService.createSession("mentor@example.com", dto));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test: Cancel session successfully updates status\n" +
            "Why: Verifies scheduled sessions can be cancelled by participants.\n" +
            "Expected: Session status transitions to CANCELLED and notifications are dispatched.")
    void cancelSession_success() {
        User mentor = TestDataFactory.createUser(1L, "mentor@example.com", "mentor", Role.USER);
        User learner = TestDataFactory.createUser(2L, "learner@example.com", "learner", Role.USER);
        ExchangeRequest request = TestDataFactory.createExchangeRequest(1L, mentor, learner, RequestStatus.ACCEPTED);
        Session session = TestDataFactory.createSession(1L, request, mentor, learner, SessionStatus.SCHEDULED);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionResponseDTO response = sessionService.cancelSession(1L, "mentor@example.com");

        assertNotNull(response);
        assertEquals(SessionStatus.CANCELLED, response.getStatus());
        verify(notificationService).createNotification(eq(learner), any(), any(),
                eq(NotificationType.SESSION_CANCELLED));
    }
}
