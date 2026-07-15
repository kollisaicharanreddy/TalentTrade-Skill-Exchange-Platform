package com.talenttrade.service;

import com.talenttrade.dto.ReviewRequestDTO;
import com.talenttrade.dto.ReviewResponseDTO;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.*;
import com.talenttrade.exception.*;
import com.talenttrade.repository.ReviewRepository;
import com.talenttrade.repository.SessionRepository;
import com.talenttrade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    @org.springframework.cache.annotation.Caching(
        evict = {
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardStats", allEntries = true),
            @org.springframework.cache.annotation.CacheEvict(value = "platformAnalytics", key = "'analytics'"),
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardSummary", key = "'summary'")
        }
    )
    public ReviewResponseDTO submitReview(String reviewerEmail, ReviewRequestDTO requestDTO) {
        log.info("User {} is submitting a review for session ID: {}", reviewerEmail, requestDTO.getSessionId());

        Session session = sessionRepository.findById(requestDTO.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException("Session not found with ID: " + requestDTO.getSessionId()));

        // Reviews can only be submitted after session completion
        if (session.getStatus() != SessionStatus.COMPLETED) {
            log.warn("Attempted to review session ID: {} in status: {}", session.getId(), session.getStatus());
            throw new InvalidSessionStateException("Reviews can only be submitted after session completion");
        }

        // Identify reviewer and reviewee
        User reviewer;
        User reviewee;

        if (session.getMentor().getEmail().equals(reviewerEmail)) {
            reviewer = session.getMentor();
            reviewee = session.getLearner();
        } else if (session.getLearner().getEmail().equals(reviewerEmail)) {
            reviewer = session.getLearner();
            reviewee = session.getMentor();
        } else {
            log.warn("User {} attempted to review session ID: {} but is not a participant", reviewerEmail, session.getId());
            throw new UnauthorizedException("You are not authorized to submit a review for this session");
        }

        // Prevent duplicate reviews
        if (reviewRepository.existsBySessionIdAndReviewerId(session.getId(), reviewer.getId())) {
            log.warn("Review already submitted by user ID: {} for session ID: {}", reviewer.getId(), session.getId());
            throw new ReviewAlreadyExistsException("You have already submitted a review for this session");
        }

        // Prevent self-reviews
        if (reviewer.getId().equals(reviewee.getId())) {
            throw new InvalidRequestException("You cannot review yourself");
        }

        Review review = Review.builder()
                .session(session)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(requestDTO.getRating())
                .comment(requestDTO.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review submitted successfully with ID: {}", savedReview.getId());

        // Notify reviewee
        notificationService.createNotification(
                reviewee,
                "New Review Received",
                reviewer.getFullName() + " left you a " + savedReview.getRating() + "-star review for your session on " + session.getScheduledDate(),
                NotificationType.REVIEW_RECEIVED
        );

        return mapToResponseDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching reviews received by user ID: {}", userId);
        
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return reviewRepository.findByRevieweeId(userId, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getMyReviews(String email, Pageable pageable) {
        log.debug("Fetching reviews received by user: {}", email);
        
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return reviewRepository.findByRevieweeEmail(email, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional
    @org.springframework.cache.annotation.Caching(
        evict = {
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardStats", allEntries = true),
            @org.springframework.cache.annotation.CacheEvict(value = "platformAnalytics", key = "'analytics'"),
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardSummary", key = "'summary'")
        }
    )
    public void deleteReview(Long id, String email) {
        log.info("User {} is attempting to delete review ID: {}", email, id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));

        if (!review.getReviewer().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to delete review ID: {} by user: {}", id, email);
            throw new UnauthorizedException("Only the reviewer can delete this review");
        }

        reviewRepository.delete(review);
        log.info("Review ID: {} deleted successfully", id);
    }

    private ReviewResponseDTO mapToResponseDTO(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .sessionId(review.getSession().getId())
                .reviewer(mapToUserResponse(review.getReviewer()))
                .reviewee(mapToUserResponse(review.getReviewee()))
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
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
