package com.talenttrade.service;

import com.talenttrade.dto.SkillDTO;
import com.talenttrade.dto.SkillResponseDTO;
import com.talenttrade.entity.Skill;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.exception.SkillAlreadyExistsException;
import com.talenttrade.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional
    public SkillResponseDTO createSkill(SkillDTO skillDTO) {
        log.info("Attempting to create skill: {}", skillDTO.getName());
        if (skillRepository.existsByName(skillDTO.getName())) {
            log.warn("Skill creation failed - skill already exists: {}", skillDTO.getName());
            throw new SkillAlreadyExistsException("Skill already exists: " + skillDTO.getName());
        }

        Skill skill = Skill.builder()
                .name(skillDTO.getName())
                .category(skillDTO.getCategory())
                .description(skillDTO.getDescription())
                .build();

        Skill savedSkill = skillRepository.save(skill);
        log.info("Skill created successfully with ID: {}, name: {}", savedSkill.getId(), savedSkill.getName());

        return mapToResponseDTO(savedSkill);
    }

    @Transactional(readOnly = true)
    public List<SkillResponseDTO> getAllSkills() {
        log.debug("Fetching all skills");
        return skillRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SkillResponseDTO getSkillById(Long id) {
        log.debug("Fetching skill with ID: {}", id);
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + id));
        return mapToResponseDTO(skill);
    }

    @Transactional
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
