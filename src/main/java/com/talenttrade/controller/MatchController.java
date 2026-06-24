package com.talenttrade.controller;

import com.talenttrade.dto.MatchResponseDTO;
import com.talenttrade.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matching Engine", description = "Endpoints for retrieving skill-based matches and triggering recalculation")
@SecurityRequirement(name = "BearerAuth")
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    @Operation(summary = "Get user matches", description = "Retrieves all matches for the currently authenticated user based on reciprocal teach and learn skills.")
    public ResponseEntity<List<MatchResponseDTO>> getMatches(Authentication authentication) {
        String email = authentication.getName();
        List<MatchResponseDTO> response = matchService.getMatchesForUser(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match details by ID", description = "Retrieves details of a specific match. Access is restricted to the match participants.")
    public ResponseEntity<MatchResponseDTO> getMatchById(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        MatchResponseDTO response = matchService.getMatchById(id, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh matches", description = "Triggers the matching engine to recalculate reciprocal matches across all users in the system.")
    public ResponseEntity<Void> refreshMatches() {
        matchService.refreshMatches();
        return ResponseEntity.ok().build();
    }
}
