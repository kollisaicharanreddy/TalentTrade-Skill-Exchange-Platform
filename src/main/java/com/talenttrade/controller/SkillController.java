package com.talenttrade.controller;

import com.talenttrade.dto.SkillDTO;
import com.talenttrade.dto.SkillResponseDTO;
import com.talenttrade.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Tag(name = "Skill Management", description = "Endpoints for creating and managing skill registry")
@SecurityRequirement(name = "BearerAuth")
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @Operation(summary = "Create a new skill", description = "Allows creation of a unique skill. Returns the created skill details.")
    public ResponseEntity<SkillResponseDTO> createSkill(@Valid @RequestBody SkillDTO skillDTO) {
        SkillResponseDTO response = skillService.createSkill(skillDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all skills", description = "Fetches a list of all skills registered in the platform.")
    public ResponseEntity<List<SkillResponseDTO>> getAllSkills() {
        List<SkillResponseDTO> response = skillService.getAllSkills();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a skill by ID", description = "Fetches details of a specific skill using its unique ID.")
    public ResponseEntity<SkillResponseDTO> getSkillById(@PathVariable Long id) {
        SkillResponseDTO response = skillService.getSkillById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a skill", description = "Removes a skill from the registry using its unique ID.")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
}
