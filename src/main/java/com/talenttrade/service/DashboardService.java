package com.talenttrade.service;

import com.talenttrade.dto.DashboardResponseDTO;
import com.talenttrade.entity.*;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final MatchRepository matchRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final SessionRepository sessionRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "dashboardStats", key = "#email")
    public DashboardResponseDTO generateDashboard(String email) {
        log.info("Generating dashboard statistics for user: {}", email);

        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        // 1 & 2. Skills statistics
        List<UserSkill> userSkills = userSkillRepository.findByUserEmail(email);
        long skillsOffered = userSkills.stream()
                .filter(s -> s.getType() == SkillType.TEACH)
                .count();
        long skillsWanted = userSkills.stream()
                .filter(s -> s.getType() == SkillType.LEARN)
                .count();

        // 3. Matches statistics
        List<Match> matchesList = matchRepository.findByUser1EmailOrUser2Email(email, email);
        long matches = matchesList.size();

        // 4, 5, 6, 7. Request statistics
        List<ExchangeRequest> requestsList = exchangeRequestRepository.findBySenderEmailOrReceiverEmail(email, email);
        long requestsSent = requestsList.stream()
                .filter(r -> r.getSender().getEmail().equals(email))
                .count();
        long requestsReceived = requestsList.size() - requestsSent;
        long acceptedRequests = requestsList.stream()
                .filter(r -> r.getStatus() == RequestStatus.ACCEPTED)
                .count();
        long rejectedRequests = requestsList.stream()
                .filter(r -> r.getStatus() == RequestStatus.REJECTED)
                .count();

        // 8 & 9. Session statistics
        long upcomingSessions = sessionRepository.countByUserAndStatus(email, SessionStatus.SCHEDULED);
        long completedSessions = sessionRepository.countByUserAndStatus(email, SessionStatus.COMPLETED);

        // 10 & 11. Review statistics
        Double avgRating = reviewRepository.getAverageRatingForUser(email);
        double averageRating = (avgRating != null) ? Math.round(avgRating * 10.0) / 10.0 : 0.0;
        long totalReviews = reviewRepository.countByRevieweeEmail(email);

        log.debug("Dashboard stats successfully computed for user {}: Offered={}, Wanted={}, Matches={}, Sent={}, Received={}, Accepted={}, Rejected={}, UpcomingSessions={}, CompletedSessions={}, AvgRating={}, ReviewsCount={}",
                email, skillsOffered, skillsWanted, matches, requestsSent, requestsReceived, acceptedRequests, rejectedRequests, upcomingSessions, completedSessions, averageRating, totalReviews);

        return DashboardResponseDTO.builder()
                .skillsOffered(skillsOffered)
                .skillsWanted(skillsWanted)
                .matches(matches)
                .requestsSent(requestsSent)
                .requestsReceived(requestsReceived)
                .acceptedRequests(acceptedRequests)
                .rejectedRequests(rejectedRequests)
                .upcomingSessions(upcomingSessions)
                .completedSessions(completedSessions)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }
}
