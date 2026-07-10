package com.talenttrade.integration;

import com.talenttrade.BaseIntegrationTest;
import com.talenttrade.dto.SkillDTO;
import com.talenttrade.dto.SkillResponseDTO;
import com.talenttrade.service.SkillService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class SkillWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SkillService skillService;

    @Test
    @DisplayName("Workflow 2 Integration: Create Skill -> Retrieve Skill")
    void createRetrieveSkillWorkflow() {
        SkillDTO skillDTO = SkillDTO.builder()
                .name("Spring Boot Integration Test Skill")
                .category("Backend")
                .description("Framework for building microservices")
                .build();

        // 1. Create Skill
        SkillResponseDTO created = skillService.createSkill(skillDTO);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Spring Boot Integration Test Skill", created.getName());

        // 2. Retrieve Skill
        SkillResponseDTO retrieved = skillService.getSkillById(created.getId());
        assertNotNull(retrieved);
        assertEquals("Spring Boot Integration Test Skill", retrieved.getName());
        assertEquals("Backend", retrieved.getCategory());
    }
}
