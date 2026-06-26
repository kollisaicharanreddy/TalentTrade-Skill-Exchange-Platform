package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
import com.talenttrade.dto.UserSkillRequestDTO;
import com.talenttrade.dto.UserSkillResponseDTO;
import com.talenttrade.service.UserSkillService;
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
@RequestMapping("/api/users/skills")
@RequiredArgsConstructor
@Tag(name = "User Skills", description = "Endpoints for managing skills an authenticated user can teach or wants to learn")
@SecurityRequirement(name = "BearerAuth")
public class UserSkillController {

    private final UserSkillService userSkillService;

    @PostMapping
    @Operation(summary = "Assign a skill to user", description = "Assigns a teach/learn skill with a level (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT) to the current authenticated user.")
    public ResponseEntity<ApiResponse<UserSkillResponseDTO>> addUserSkill(
            @Valid @RequestBody UserSkillRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserSkillResponseDTO response = userSkillService.addUserSkill(email, request);
        return new ResponseEntity<>(ApiResponse.success(response, "Skill assigned to user profile successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get user skills", description = "Fetches all skills (both teach and learn) assigned to the current authenticated user.")
    public ResponseEntity<ApiResponse<List<UserSkillResponseDTO>>> getUserSkills(Authentication authentication) {
        String email = authentication.getName();
        List<UserSkillResponseDTO> response = userSkillService.getUserSkills(email);
        return ResponseEntity.ok(ApiResponse.success(response, "User skills retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove user skill", description = "Removes a skill assignment from the current user profile by its association ID.")
    public ResponseEntity<ApiResponse<Void>> removeUserSkill(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        userSkillService.removeUserSkill(email, id);
        return ResponseEntity.ok(ApiResponse.success(null, "User skill association removed successfully"));
    }
}
