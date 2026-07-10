package com.talenttrade.service;

import com.talenttrade.dto.SkillDTO;
import com.talenttrade.dto.SkillResponseDTO;
import com.talenttrade.entity.Skill;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.repository.SkillRepository;
import com.talenttrade.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    @Test
    @DisplayName("Test: Successful creation of a new skill\n" +
                 "Why: Verifies a new skill is correctly saved to repository after trimming.\n" +
                 "Expected: Returns the created Skill DTO details.")
    void createSkill_new_success() {
        SkillDTO dto = SkillDTO.builder()
                .name(" Java ")
                .category("Programming")
                .description("Java Programming Skill")
                .build();

        Skill savedSkill = TestDataFactory.createSkill(1L, "Java", "Programming");

        when(skillRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenReturn(savedSkill);

        SkillResponseDTO result = skillService.createSkill(dto);

        assertNotNull(result);
        assertEquals("Java", result.getName());
        assertEquals("Programming", result.getCategory());
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    @DisplayName("Test: Create skill returns existing skill when case-insensitive match is found\n" +
                 "Why: Prevents redundant/duplicate entries from being added to system database.\n" +
                 "Expected: Returns the existing skill without executing save.")
    void createSkill_existing_returnsExisting() {
        SkillDTO dto = SkillDTO.builder()
                .name("java")
                .category("Programming")
                .description("Java Programming Skill")
                .build();

        Skill existingSkill = TestDataFactory.createSkill(1L, "Java", "Programming");

        when(skillRepository.findByNameIgnoreCase("java")).thenReturn(Optional.of(existingSkill));

        SkillResponseDTO result = skillService.createSkill(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java", result.getName());
        verify(skillRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test: Get skill by ID success\n" +
                 "Why: Validates retrieving a single skill.\n" +
                 "Expected: Skill matching the ID is mapped to response and returned.")
    void getSkillById_success() {
        Skill skill = TestDataFactory.createSkill(1L, "Java", "Programming");
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));

        SkillResponseDTO result = skillService.getSkillById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java", result.getName());
    }

    @Test
    @DisplayName("Test: Get skill by ID throws error if skill not found\n" +
                 "Why: Verifies resource-not-found handler trigger.\n" +
                 "Expected: ResourceNotFoundException is thrown.")
    void getSkillById_notFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> skillService.getSkillById(99L));
    }

    @Test
    @DisplayName("Test: Delete skill successfully\n" +
                 "Why: Verifies skill removal operations from repository.\n" +
                 "Expected: Skill is loaded and delete is called.")
    void deleteSkill_success() {
        Skill skill = TestDataFactory.createSkill(1L, "Java", "Programming");
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));

        skillService.deleteSkill(1L);

        verify(skillRepository).delete(skill);
    }
}
