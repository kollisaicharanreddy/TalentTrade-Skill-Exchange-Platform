package com.talenttrade.controller;

import com.talenttrade.dto.ExchangeRequestDTO;
import com.talenttrade.dto.ExchangeRequestResponseDTO;
import com.talenttrade.service.ExchangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "Exchange Requests", description = "Endpoints for sending, receiving, and managing skill exchange requests")
@SecurityRequirement(name = "BearerAuth")
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;

    @PostMapping
    @Operation(summary = "Send an exchange request", description = "Allows an authenticated user to send a skill exchange request to another user. Message and receiver details are validated.")
    public ResponseEntity<ExchangeRequestResponseDTO> createRequest(
            @Valid @RequestBody ExchangeRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        ExchangeRequestResponseDTO response = exchangeRequestService.createRequest(email, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all requests", description = "Retrieves all requests (both sent and received) involving the current authenticated user.")
    public ResponseEntity<List<ExchangeRequestResponseDTO>> getAllRequests(Authentication authentication) {
        String email = authentication.getName();
        List<ExchangeRequestResponseDTO> response = exchangeRequestService.getAllRequests(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sent")
    @Operation(summary = "Get sent requests", description = "Retrieves all exchange requests sent by the current authenticated user.")
    public ResponseEntity<List<ExchangeRequestResponseDTO>> getSentRequests(Authentication authentication) {
        String email = authentication.getName();
        List<ExchangeRequestResponseDTO> response = exchangeRequestService.getSentRequests(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/received")
    @Operation(summary = "Get received requests", description = "Retrieves all exchange requests received by the current authenticated user.")
    public ResponseEntity<List<ExchangeRequestResponseDTO>> getReceivedRequests(Authentication authentication) {
        String email = authentication.getName();
        List<ExchangeRequestResponseDTO> response = exchangeRequestService.getReceivedRequests(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept an exchange request", description = "Allows the receiver of the request to accept it. The request state changes to ACCEPTED.")
    public ResponseEntity<ExchangeRequestResponseDTO> acceptRequest(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        ExchangeRequestResponseDTO response = exchangeRequestService.acceptRequest(id, email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject an exchange request", description = "Allows the receiver of the request to reject it. The request state changes to REJECTED.")
    public ResponseEntity<ExchangeRequestResponseDTO> rejectRequest(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        ExchangeRequestResponseDTO response = exchangeRequestService.rejectRequest(id, email);
        return ResponseEntity.ok(response);
    }
}
