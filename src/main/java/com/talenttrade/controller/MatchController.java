package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
import com.talenttrade.dto.MatchResponseDTO;
import com.talenttrade.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matching Engine", description = "Endpoints for retrieving skill-based matches and triggering recalculation")
@SecurityRequirement(name = "BearerAuth")
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    @Operation(summary = "Get user matches", description = "Retrieves a paginated list of matches for the currently authenticated user based on reciprocal teach and learn skills.")
    public ResponseEntity<ApiResponse<Page<MatchResponseDTO>>> getMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<MatchResponseDTO> response = matchService.getMatchesForUser(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Matches retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match details by ID", description = "Retrieves details of a specific match. Access is restricted to the match participants.")
    public ResponseEntity<ApiResponse<MatchResponseDTO>> getMatchById(
            @PathVariable Long id, 
            Authentication authentication
    ) {
        String email = authentication.getName();
        MatchResponseDTO response = matchService.getMatchById(id, email);
        return ResponseEntity.ok(ApiResponse.success(response, "Match details retrieved successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh matches", description = "Triggers the matching engine to recalculate reciprocal matches across all users in the system.")
    public ResponseEntity<ApiResponse<Void>> refreshMatches() {
        matchService.refreshMatches();
        return ResponseEntity.ok(ApiResponse.success(null, "Matches recalculated successfully"));
    }
}
