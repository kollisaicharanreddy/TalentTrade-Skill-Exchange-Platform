package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints for managing authenticated user profiles and user lists")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;
    private final UserSkillService userSkillService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Fetches the profile details of the currently authenticated user using JWT credentials.")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        UserResponse response = userService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully"));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates full name, bio, and location details for the currently authenticated user.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserResponse response = userService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users registered on the platform with pagination and sorting support.")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Users retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by skill", description = "Searches for users who can teach a specific skill with pagination and sorting support.")
    public ResponseEntity<ApiResponse<Page<UserSkillSearchResponseDTO>>> searchUsersBySkill(
            @RequestParam String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "level") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<UserSkillSearchResponseDTO> response = userSkillService.searchUsersBySkill(skill, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "User search completed successfully"));
    }
}
