package com.talenttrade.service;

import com.talenttrade.dto.SkillDTO;
import com.talenttrade.dto.SkillResponseDTO;
import com.talenttrade.entity.Skill;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional
    @org.springframework.cache.annotation.Caching(
        evict = {
            @org.springframework.cache.annotation.CacheEvict(value = "platformAnalytics", key = "'analytics'"),
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardSummary", key = "'summary'")
        }
    )
    public SkillResponseDTO createSkill(SkillDTO skillDTO) {
        log.info("Attempting to create skill: {}", skillDTO.getName());
        String nameTrimmed = skillDTO.getName().trim();
        
        java.util.Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(nameTrimmed);
        if (existingSkill.isPresent()) {
            log.info("Skill already exists case-insensitively. Returning existing skill details for: {}", nameTrimmed);
            return mapToResponseDTO(existingSkill.get());
        }

        Skill skill = Skill.builder()
                .name(nameTrimmed)
                .category(skillDTO.getCategory())
                .description(skillDTO.getDescription())
                .build();

        Skill savedSkill = skillRepository.save(skill);
        log.info("Skill created successfully with ID: {}, name: {}", savedSkill.getId(), savedSkill.getName());

        return mapToResponseDTO(savedSkill);
    }

    @Transactional(readOnly = true)
    public Page<SkillResponseDTO> getAllSkills(Pageable pageable) {
        log.debug("Fetching all skills with pagination");
        return skillRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public SkillResponseDTO getSkillById(Long id) {
        log.debug("Fetching skill with ID: {}", id);
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + id));
        return mapToResponseDTO(skill);
    }

    @Transactional
    @org.springframework.cache.annotation.Caching(
        evict = {
            @org.springframework.cache.annotation.CacheEvict(value = "platformAnalytics", key = "'analytics'"),
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardSummary", key = "'summary'")
        }
    )
    public void deleteSkill(Long id) {
        log.info("Attempting to delete skill with ID: {}", id);
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + id));
        skillRepository.delete(skill);
        log.info("Skill with ID: {} deleted successfully", id);
    }

    public SkillResponseDTO mapToResponseDTO(Skill skill) {
        return SkillResponseDTO.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .description(skill.getDescription())
                .build();
    }
}
