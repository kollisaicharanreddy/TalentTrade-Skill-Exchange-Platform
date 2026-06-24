package com.talenttrade.controller;

import com.talenttrade.dto.UpdateProfileRequest;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.dto.UserSkillSearchResponseDTO;
import com.talenttrade.service.UserService;
import com.talenttrade.service.UserSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints for managing authenticated user profiles")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;
    private final UserSkillService userSkillService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Fetches the profile details of the currently authenticated user using JWT credentials.")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        UserResponse response = userService.getProfile(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates full name, bio, and location details for the currently authenticated user.")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserResponse response = userService.updateProfile(email, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by skill", description = "Searches for users who can teach a specific skill with pagination support.")
    public ResponseEntity<Page<UserSkillSearchResponseDTO>> searchUsersBySkill(
            @RequestParam String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserSkillSearchResponseDTO> response = userSkillService.searchUsersBySkill(skill, pageable);
        return ResponseEntity.ok(response);
    }
}
