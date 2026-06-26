package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
import com.talenttrade.dto.ExchangeRequestDTO;
import com.talenttrade.dto.ExchangeRequestResponseDTO;
import com.talenttrade.service.ExchangeRequestService;
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
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "Exchange Requests", description = "Endpoints for sending, receiving, and managing skill exchange requests")
@SecurityRequirement(name = "BearerAuth")
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;

    @PostMapping
    @Operation(summary = "Send an exchange request", description = "Allows an authenticated user to send a skill exchange request to another user. Message and receiver details are validated.")
    public ResponseEntity<ApiResponse<ExchangeRequestResponseDTO>> createRequest(
            @Valid @RequestBody ExchangeRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        ExchangeRequestResponseDTO response = exchangeRequestService.createRequest(email, request);
        return new ResponseEntity<>(ApiResponse.success(response, "Exchange request sent successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all requests", description = "Retrieves all requests (both sent and received) involving the current authenticated user with pagination and sorting support.")
    public ResponseEntity<ApiResponse<Page<ExchangeRequestResponseDTO>>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ExchangeRequestResponseDTO> response = exchangeRequestService.getAllRequests(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "All exchange requests retrieved successfully"));
    }

    @GetMapping("/sent")
    @Operation(summary = "Get sent requests", description = "Retrieves all exchange requests sent by the current authenticated user with pagination and sorting support.")
    public ResponseEntity<ApiResponse<Page<ExchangeRequestResponseDTO>>> getSentRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ExchangeRequestResponseDTO> response = exchangeRequestService.getSentRequests(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Sent exchange requests retrieved successfully"));
    }

    @GetMapping("/received")
    @Operation(summary = "Get received requests", description = "Retrieves all exchange requests received by the current authenticated user with pagination and sorting support.")
    public ResponseEntity<ApiResponse<Page<ExchangeRequestResponseDTO>>> getReceivedRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ExchangeRequestResponseDTO> response = exchangeRequestService.getReceivedRequests(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Received exchange requests retrieved successfully"));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept an exchange request", description = "Allows the receiver of the request to accept it. The request state changes to ACCEPTED.")
    public ResponseEntity<ApiResponse<ExchangeRequestResponseDTO>> acceptRequest(
            @PathVariable Long id, 
            Authentication authentication
    ) {
        String email = authentication.getName();
        ExchangeRequestResponseDTO response = exchangeRequestService.acceptRequest(id, email);
        return ResponseEntity.ok(ApiResponse.success(response, "Exchange request accepted successfully"));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject an exchange request", description = "Allows the receiver of the request to reject it. The request state changes to REJECTED.")
    public ResponseEntity<ApiResponse<ExchangeRequestResponseDTO>> rejectRequest(
            @PathVariable Long id, 
            Authentication authentication
    ) {
        String email = authentication.getName();
        ExchangeRequestResponseDTO response = exchangeRequestService.rejectRequest(id, email);
        return ResponseEntity.ok(ApiResponse.success(response, "Exchange request rejected successfully"));
    }
}
