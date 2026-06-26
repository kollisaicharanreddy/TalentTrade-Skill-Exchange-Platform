package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
import com.talenttrade.dto.SessionRequestDTO;
import com.talenttrade.dto.SessionResponseDTO;
import com.talenttrade.service.SessionService;
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
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Endpoints for scheduling and managing skill exchange sessions")
@SecurityRequirement(name = "BearerAuth")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "Schedule a new session", description = "Creates a new scheduled session for an accepted skill exchange request. Date and time conflict checks are performed.")
    public ResponseEntity<ApiResponse<SessionResponseDTO>> createSession(
            @Valid @RequestBody SessionRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        SessionResponseDTO response = sessionService.createSession(email, request);
        return new ResponseEntity<>(ApiResponse.success(response, "Session scheduled successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all sessions", description = "Retrieves all sessions involving the current authenticated user (both mentor and learner roles) with pagination and sorting support.")
    public ResponseEntity<ApiResponse<Page<SessionResponseDTO>>> getSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<SessionResponseDTO> response = sessionService.getSessions(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Sessions retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session details", description = "Retrieves metadata of a specific session by its ID. Only the mentor or learner of the session can access it.")
    public ResponseEntity<ApiResponse<SessionResponseDTO>> getSessionDetails(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        SessionResponseDTO response = sessionService.getSessionDetails(id, email);
        return ResponseEntity.ok(ApiResponse.success(response, "Session details retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update session details", description = "Allows session participants to update the schedule, meeting link, and notes. Edits are prevented if the session is not in a SCHEDULED state.")
    public ResponseEntity<ApiResponse<SessionResponseDTO>> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody SessionRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        SessionResponseDTO response = sessionService.updateSession(id, email, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Session updated successfully"));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Mark session as completed", description = "Sets the session status to COMPLETED. Only participants can trigger this.")
    public ResponseEntity<ApiResponse<SessionResponseDTO>> completeSession(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        SessionResponseDTO response = sessionService.completeSession(id, email);
        return ResponseEntity.ok(ApiResponse.success(response, "Session marked as completed successfully"));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a session", description = "Sets the session status to CANCELLED and dispatches a notification to the other participant. Only scheduled sessions can be cancelled.")
    public ResponseEntity<ApiResponse<SessionResponseDTO>> cancelSession(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        SessionResponseDTO response = sessionService.cancelSession(id, email);
        return ResponseEntity.ok(ApiResponse.success(response, "Session cancelled successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a session", description = "Removes a session from the system entirely. Restricted to session participants.")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        sessionService.deleteSession(id, email);
        return ResponseEntity.ok(ApiResponse.success(null, "Session deleted successfully"));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming sessions", description = "Retrieves upcoming scheduled sessions involving the current authenticated user with pagination and sorting support.")
    public ResponseEntity<ApiResponse<Page<SessionResponseDTO>>> getUpcomingSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<SessionResponseDTO> response = sessionService.getUpcomingSessions(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Upcoming sessions retrieved successfully"));
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed sessions", description = "Retrieves all completed sessions involving the current authenticated user with pagination and sorting support.")
    public ResponseEntity<ApiResponse<Page<SessionResponseDTO>>> getCompletedSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<SessionResponseDTO> response = sessionService.getCompletedSessions(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Completed sessions retrieved successfully"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get session history", description = "Retrieves all sessions (scheduled, completed, and cancelled) involving the current authenticated user with pagination and sorting support.")
    public ResponseEntity<ApiResponse<Page<SessionResponseDTO>>> getSessionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<SessionResponseDTO> response = sessionService.getSessionHistory(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Session history retrieved successfully"));
    }
}
