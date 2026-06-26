package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
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
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> submitReview(
            @Valid @RequestBody ReviewRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        ReviewResponseDTO response = reviewService.submitReview(email, request);
        return new ResponseEntity<>(ApiResponse.success(response, "Review submitted successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews for user", description = "Retrieves a paginated and sorted list of reviews received by a specific user by their user ID.")
    public ResponseEntity<ApiResponse<Page<ReviewResponseDTO>>> getReviewsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ReviewResponseDTO> response = reviewService.getReviewsForUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "User reviews retrieved successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my reviews", description = "Retrieves a paginated and sorted list of reviews received by the currently authenticated user.")
    public ResponseEntity<ApiResponse<Page<ReviewResponseDTO>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ReviewResponseDTO> response = reviewService.getMyReviews(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "My reviews retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review", description = "Removes a review from the database. Allowed only for the user who wrote the review.")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        reviewService.deleteReview(id, email);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }
}
