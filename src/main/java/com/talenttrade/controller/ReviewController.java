package com.talenttrade.controller;

import com.talenttrade.dto.ReviewRequestDTO;
import com.talenttrade.dto.ReviewResponseDTO;
import com.talenttrade.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Endpoints for leaving and viewing session reviews and ratings")
@SecurityRequirement(name = "BearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Submit a review", description = "Submits a rating and comment for a completed session. Reviews are verified to prevent duplicates and self-reviews. The other participant is automatically set as the reviewee.")
    public ResponseEntity<ReviewResponseDTO> submitReview(
            @Valid @RequestBody ReviewRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        ReviewResponseDTO response = reviewService.submitReview(email, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews for user", description = "Retrieves a paginated list of reviews received by a specific user by their user ID.")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponseDTO> response = reviewService.getReviewsForUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get my reviews", description = "Retrieves a paginated list of reviews received by the currently authenticated user.")
    public ResponseEntity<Page<ReviewResponseDTO>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponseDTO> response = reviewService.getMyReviews(email, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review", description = "Removes a review from the database. Allowed only for the user who wrote the review.")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        reviewService.deleteReview(id, email);
        return ResponseEntity.noContent().build();
    }
}
