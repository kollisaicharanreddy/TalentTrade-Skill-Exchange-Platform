package com.talenttrade.service;

import com.talenttrade.dto.SkillResponseDTO;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.dto.UserSkillRequestDTO;
import com.talenttrade.dto.UserSkillResponseDTO;
import com.talenttrade.dto.UserSkillSearchResponseDTO;
import com.talenttrade.entity.Skill;
import com.talenttrade.entity.User;
import com.talenttrade.entity.UserSkill;
import com.talenttrade.exception.DuplicateResourceException;
import com.talenttrade.exception.ResourceNotFoundException;
import com.talenttrade.exception.UnauthorizedException;
import com.talenttrade.repository.SkillRepository;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    @Transactional
    @org.springframework.cache.annotation.Caching(
        evict = {
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardStats", key = "#email"),
            @org.springframework.cache.annotation.CacheEvict(value = "platformAnalytics", key = "'analytics'"),
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardSummary", key = "'summary'"),
            @org.springframework.cache.annotation.CacheEvict(value = "matchResults", allEntries = true)
        }
    )
    public UserSkillResponseDTO addUserSkill(String email, UserSkillRequestDTO request) {
        log.info("Attempting to assign skill ID: {} to user: {} as {}", request.getSkillId(), email, request.getType());
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + request.getSkillId()));

        if (userSkillRepository.existsByUserAndSkillAndType(user, skill, request.getType())) {
            log.warn("Skill assignment failed - user {} already has skill {} with type {}", email, skill.getName(), request.getType());
            throw new DuplicateResourceException("Skill already assigned to user with type " + request.getType());
        }

        UserSkill userSkill = UserSkill.builder()
                .user(user)
                .skill(skill)
                .type(request.getType())
                .level(request.getLevel())
                .build();

        UserSkill savedUserSkill = userSkillRepository.save(userSkill);
        log.info("Skill assigned successfully: user={}, skill={}, type={}, level={}", 
                email, skill.getName(), request.getType(), request.getLevel());

        return mapToResponseDTO(savedUserSkill);
    }

    @Transactional(readOnly = true)
    public List<UserSkillResponseDTO> getUserSkills(String email) {
        log.debug("Fetching skills for user: {}", email);
        return userSkillRepository.findByUserEmail(email).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @org.springframework.cache.annotation.Caching(
        evict = {
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardStats", key = "#email"),
            @org.springframework.cache.annotation.CacheEvict(value = "platformAnalytics", key = "'analytics'"),
            @org.springframework.cache.annotation.CacheEvict(value = "dashboardSummary", key = "'summary'"),
            @org.springframework.cache.annotation.CacheEvict(value = "matchResults", allEntries = true)
        }
    )
    public void removeUserSkill(String email, Long userSkillId) {
        log.info("Attempting to remove user skill ID: {} for user: {}", userSkillId, email);
        
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found with ID: " + userSkillId));

        if (!userSkill.getUser().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to remove user skill ID: {} by user: {}", userSkillId, email);
            throw new UnauthorizedException("You do not have permission to remove this skill");
        }

        userSkillRepository.delete(userSkill);
        log.info("User skill ID: {} removed successfully for user: {}", userSkillId, email);
    }

    @Transactional(readOnly = true)
    public Page<UserSkillSearchResponseDTO> searchUsersBySkill(String skillName, Pageable pageable) {
        log.info("Searching users by skill name containing: {}", skillName);
        // Defaulting to TEACH type to find mentors/teachers who have the skill
        Page<UserSkill> userSkills = userSkillRepository.findBySkillNameContainingIgnoreCaseAndType(
                skillName, com.talenttrade.entity.SkillType.TEACH, pageable);
        return userSkills.map(this::mapToSearchResponseDTO);
    }

    private UserSkillResponseDTO mapToResponseDTO(UserSkill userSkill) {
        UserResponse userResponse = mapToUserResponse(userSkill.getUser());
        SkillResponseDTO skillResponse = mapToSkillResponse(userSkill.getSkill());

        return UserSkillResponseDTO.builder()
                .id(userSkill.getId())
                .user(userResponse)
                .skill(skillResponse)
                .type(userSkill.getType())
                .level(userSkill.getLevel())
                .build();
    }

    private UserSkillSearchResponseDTO mapToSearchResponseDTO(UserSkill userSkill) {
        return UserSkillSearchResponseDTO.builder()
                .user(mapToUserResponse(userSkill.getUser()))
                .skill(mapToSkillResponse(userSkill.getSkill()))
                .level(userSkill.getLevel())
                .type(userSkill.getType())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsernameValue())
                .email(user.getEmail())
                .bio(user.getBio())
                .location(user.getLocation())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private SkillResponseDTO mapToSkillResponse(Skill skill) {
        return SkillResponseDTO.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .description(skill.getDescription())
                .build();
    }
}
