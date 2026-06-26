package com.talenttrade.service;

import com.talenttrade.dto.SessionRequestDTO;
import com.talenttrade.dto.SessionResponseDTO;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.ExchangeRequest;
import com.talenttrade.entity.NotificationType;
import com.talenttrade.entity.RequestStatus;
import com.talenttrade.entity.Session;
import com.talenttrade.entity.SessionStatus;
import com.talenttrade.entity.User;
import com.talenttrade.exception.InvalidRequestException;
import com.talenttrade.exception.InvalidSessionStateException;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.exception.SessionNotFoundException;
import com.talenttrade.exception.UnauthorizedException;
import com.talenttrade.repository.ExchangeRequestRepository;
import com.talenttrade.repository.SessionRepository;
import com.talenttrade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public SessionResponseDTO createSession(String schedulerEmail, SessionRequestDTO requestDTO) {
        log.info("User {} is scheduling a session for exchange request ID: {}", schedulerEmail, requestDTO.getExchangeRequestId());

        ExchangeRequest request = exchangeRequestRepository.findById(requestDTO.getExchangeRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found with ID: " + requestDTO.getExchangeRequestId()));

        if (request.getStatus() != RequestStatus.ACCEPTED) {
            log.warn("Attempted to schedule session for request ID: {} in status: {}", request.getId(), request.getStatus());
            throw new InvalidSessionStateException("Session can only be scheduled for accepted exchange requests");
        }

        // Enforce 1-to-1 relationship between ExchangeRequest and Session
        if (sessionRepository.existsByExchangeRequestId(request.getId())) {
            log.warn("Session already scheduled for exchange request ID: {}", request.getId());
            throw new InvalidSessionStateException("A session has already been scheduled for this exchange request");
        }

        User mentor = userRepository.findById(requestDTO.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor user not found with ID: " + requestDTO.getMentorId()));

        User learner = userRepository.findById(requestDTO.getLearnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Learner user not found with ID: " + requestDTO.getLearnerId()));

        // Verify scheduler is participant
        if (!request.getSender().getEmail().equals(schedulerEmail) && !request.getReceiver().getEmail().equals(schedulerEmail)) {
            log.warn("User {} attempted to schedule session but is not participant in request ID: {}", schedulerEmail, request.getId());
            throw new UnauthorizedException("Only participants of the exchange request can schedule a session");
        }

        // Verify mentor and learner are indeed the request participants
        boolean participantsMatch = (request.getSender().getId().equals(mentor.getId()) && request.getReceiver().getId().equals(learner.getId())) ||
                                     (request.getSender().getId().equals(learner.getId()) && request.getReceiver().getId().equals(mentor.getId()));
        if (!participantsMatch) {
            throw new InvalidRequestException("Mentor and learner must be the participants of the exchange request");
        }

        // Validate scheduled date and time
        if (requestDTO.getScheduledDate().isBefore(LocalDate.now())) {
            throw new InvalidSessionStateException("Scheduled date cannot be in the past");
        }

        if (requestDTO.getEndTime().isBefore(requestDTO.getStartTime()) || requestDTO.getEndTime().equals(requestDTO.getStartTime())) {
            throw new InvalidSessionStateException("Session end time must be after start time");
        }

        // Conflict check
        if (sessionRepository.hasTimeConflict(mentor.getId(), requestDTO.getScheduledDate(), requestDTO.getStartTime(), requestDTO.getEndTime())) {
            throw new InvalidSessionStateException("Mentor has a scheduling conflict at this date and time");
        }

        if (sessionRepository.hasTimeConflict(learner.getId(), requestDTO.getScheduledDate(), requestDTO.getStartTime(), requestDTO.getEndTime())) {
            throw new InvalidSessionStateException("Learner has a scheduling conflict at this date and time");
        }

        Session session = Session.builder()
                .exchangeRequest(request)
                .mentor(mentor)
                .learner(learner)
                .scheduledDate(requestDTO.getScheduledDate())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .meetingLink(requestDTO.getMeetingLink())
                .status(SessionStatus.SCHEDULED)
                .notes(requestDTO.getNotes())
                .build();

        Session savedSession = sessionRepository.save(session);
        log.info("Session created successfully with ID: {}", savedSession.getId());

        // Notify both participants
        notificationService.createNotification(
                mentor,
                "Session Scheduled",
                "A skill exchange session has been scheduled with learner " + learner.getFullName() + " on " + savedSession.getScheduledDate() + " at " + savedSession.getStartTime(),
                NotificationType.SESSION_CREATED
        );

        notificationService.createNotification(
                learner,
                "Session Scheduled",
                "A skill exchange session has been scheduled with mentor " + mentor.getFullName() + " on " + savedSession.getScheduledDate() + " at " + savedSession.getStartTime(),
                NotificationType.SESSION_CREATED
        );

        return mapToResponseDTO(savedSession);
    }

    @Transactional
    public SessionResponseDTO updateSession(Long id, String email, SessionRequestDTO requestDTO) {
        log.info("User {} is attempting to update session ID: {}", email, id);

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("Session not found with ID: " + id));

        // Only participants can update
        if (!session.getMentor().getEmail().equals(email) && !session.getLearner().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to update session ID: {} by user: {}", id, email);
            throw new UnauthorizedException("Only session participants can update session details");
        }

        // Completed sessions cannot be edited
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            log.warn("Cannot edit session ID: {} in status: {}", id, session.getStatus());
            throw new InvalidSessionStateException("Only scheduled sessions can be updated. Current status is " + session.getStatus());
        }

        // Validate scheduled date and time
        if (requestDTO.getScheduledDate().isBefore(LocalDate.now())) {
            throw new InvalidSessionStateException("Scheduled date cannot be in the past");
        }

        if (requestDTO.getEndTime().isBefore(requestDTO.getStartTime()) || requestDTO.getEndTime().equals(requestDTO.getStartTime())) {
            throw new InvalidSessionStateException("Session end time must be after start time");
        }

        // Check scheduling conflicts (excluding the current session)
        if (sessionRepository.hasTimeConflictExcludingSession(id, session.getMentor().getId(), requestDTO.getScheduledDate(), requestDTO.getStartTime(), requestDTO.getEndTime())) {
            throw new InvalidSessionStateException("Mentor has a scheduling conflict at this date and time");
        }

        if (sessionRepository.hasTimeConflictExcludingSession(id, session.getLearner().getId(), requestDTO.getScheduledDate(), requestDTO.getStartTime(), requestDTO.getEndTime())) {
            throw new InvalidSessionStateException("Learner has a scheduling conflict at this date and time");
        }

        // Update fields
        session.setScheduledDate(requestDTO.getScheduledDate());
        session.setStartTime(requestDTO.getStartTime());
        session.setEndTime(requestDTO.getEndTime());
        session.setMeetingLink(requestDTO.getMeetingLink());
        session.setNotes(requestDTO.getNotes());

        Session updatedSession = sessionRepository.save(session);
        log.info("Session ID: {} updated successfully by user: {}", id, email);

        return mapToResponseDTO(updatedSession);
    }

    @Transactional
    public SessionResponseDTO completeSession(Long id, String email) {
        log.info("Marking session ID: {} as COMPLETED by user: {}", id, email);

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("Session not found with ID: " + id));

        if (!session.getMentor().getEmail().equals(email) && !session.getLearner().getEmail().equals(email)) {
            throw new UnauthorizedException("Only session participants can mark a session as completed");
        }

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new InvalidSessionStateException("Only scheduled sessions can be completed");
        }

        session.setStatus(SessionStatus.COMPLETED);
        Session savedSession = sessionRepository.save(session);
        log.info("Session ID: {} marked as COMPLETED successfully", id);

        return mapToResponseDTO(savedSession);
    }

    @Transactional
    public SessionResponseDTO cancelSession(Long id, String email) {
        log.info("Cancelling session ID: {} by user: {}", id, email);

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("Session not found with ID: " + id));

        if (!session.getMentor().getEmail().equals(email) && !session.getLearner().getEmail().equals(email)) {
            throw new UnauthorizedException("Only session participants can cancel the session");
        }

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new InvalidSessionStateException("Only scheduled sessions can be cancelled");
        }

        session.setStatus(SessionStatus.CANCELLED);
        Session savedSession = sessionRepository.save(session);
        log.info("Session ID: {} marked as CANCELLED successfully", id);

        // Notify the other participant
        User otherUser = session.getMentor().getEmail().equals(email) ? session.getLearner() : session.getMentor();
        User actorUser = session.getMentor().getEmail().equals(email) ? session.getMentor() : session.getLearner();

        notificationService.createNotification(
                otherUser,
                "Session Cancelled",
                "Your scheduled session on " + session.getScheduledDate() + " was cancelled by " + actorUser.getFullName(),
                NotificationType.SESSION_CANCELLED
        );

        return mapToResponseDTO(savedSession);
    }

    @Transactional
    public void deleteSession(Long id, String email) {
        log.info("Deleting session ID: {} by user: {}", id, email);

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("Session not found with ID: " + id));

        if (!session.getMentor().getEmail().equals(email) && !session.getLearner().getEmail().equals(email)) {
            throw new UnauthorizedException("Only session participants can delete a session");
        }

        sessionRepository.delete(session);
        log.info("Session ID: {} deleted successfully", id);
    }

    @Transactional(readOnly = true)
    public SessionResponseDTO getSessionDetails(Long id, String email) {
        log.debug("Fetching session details for ID: {}", id);

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("Session not found with ID: " + id));

        if (!session.getMentor().getEmail().equals(email) && !session.getLearner().getEmail().equals(email)) {
            throw new UnauthorizedException("Only session participants can view session details");
        }

        return mapToResponseDTO(session);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponseDTO> getSessions(String email, Pageable pageable) {
        log.debug("Fetching all sessions for user: {}", email);
        return sessionRepository.findByMentorEmailOrLearnerEmail(email, email, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponseDTO> getUpcomingSessions(String email, Pageable pageable) {
        log.debug("Fetching upcoming sessions for user: {}", email);
        return sessionRepository.findByUserAndStatus(email, SessionStatus.SCHEDULED, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponseDTO> getCompletedSessions(String email, Pageable pageable) {
        log.debug("Fetching completed sessions for user: {}", email);
        return sessionRepository.findByUserAndStatus(email, SessionStatus.COMPLETED, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponseDTO> getSessionHistory(String email, Pageable pageable) {
        log.debug("Fetching session history for user: {}", email);
        return sessionRepository.findByMentorEmailOrLearnerEmail(email, email, pageable)
                .map(this::mapToResponseDTO);
    }

    private SessionResponseDTO mapToResponseDTO(Session session) {
        return SessionResponseDTO.builder()
                .id(session.getId())
                .exchangeRequestId(session.getExchangeRequest().getId())
                .mentor(mapToUserResponse(session.getMentor()))
                .learner(mapToUserResponse(session.getLearner()))
                .scheduledDate(session.getScheduledDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .meetingLink(session.getMeetingLink())
                .status(session.getStatus())
                .notes(session.getNotes())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
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
