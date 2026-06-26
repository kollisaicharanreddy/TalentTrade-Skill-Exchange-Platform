package com.talenttrade.controller;

import com.talenttrade.dto.ApiResponse;
import com.talenttrade.dto.SkillDTO;
import com.talenttrade.dto.SkillResponseDTO;
import com.talenttrade.service.SkillService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Tag(name = "Skill Management", description = "Endpoints for creating and managing skill registry")
@SecurityRequirement(name = "BearerAuth")
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @Operation(summary = "Create a new skill", description = "Allows creation of a unique skill. Returns the created skill details.")
    public ResponseEntity<ApiResponse<SkillResponseDTO>> createSkill(@Valid @RequestBody SkillDTO skillDTO) {
        SkillResponseDTO response = skillService.createSkill(skillDTO);
        return new ResponseEntity<>(ApiResponse.success(response, "Skill created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all skills", description = "Fetches a paginated list of all skills registered in the platform.")
    public ResponseEntity<ApiResponse<Page<SkillResponseDTO>>> getAllSkills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<SkillResponseDTO> response = skillService.getAllSkills(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Skills retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a skill by ID", description = "Fetches details of a specific skill using its unique ID.")
    public ResponseEntity<ApiResponse<SkillResponseDTO>> getSkillById(@PathVariable Long id) {
        SkillResponseDTO response = skillService.getSkillById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Skill details retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a skill", description = "Removes a skill from the registry using its unique ID.")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Skill deleted successfully"));
    }
}
