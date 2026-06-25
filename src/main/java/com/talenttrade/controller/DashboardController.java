package com.talenttrade.controller;

import com.talenttrade.dto.DashboardResponseDTO;
import com.talenttrade.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for viewing personalized user dashboard statistics")
@SecurityRequirement(name = "BearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get user dashboard statistics", description = "Fetches comprehensive metrics for the authenticated user, including total skills, matches, request counts, session counts, and average rating.")
    public ResponseEntity<DashboardResponseDTO> getDashboard(Authentication authentication) {
        String email = authentication.getName();
        DashboardResponseDTO response = dashboardService.generateDashboard(email);
        return ResponseEntity.ok(response);
    }
}
