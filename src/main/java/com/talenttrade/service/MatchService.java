package com.talenttrade.service;

import com.talenttrade.dto.MatchResponseDTO;
import com.talenttrade.dto.UserResponse;
import com.talenttrade.entity.Match;
import com.talenttrade.entity.SkillType;
import com.talenttrade.entity.User;
import com.talenttrade.entity.UserSkill;
import com.talenttrade.exception.MatchNotFoundException;
import com.talenttrade.exception.UnauthorizedException;
import com.talenttrade.repository.MatchRepository;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    @Transactional(readOnly = true)
    public Page<MatchResponseDTO> getMatchesForUser(String email, Pageable pageable) {
        log.info("Fetching matches for user: {} with pagination", email);
        return matchRepository.findByUser1EmailOrUser2Email(email, email, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public MatchResponseDTO getMatchById(Long id, String email) {
        log.info("Fetching match ID: {} for user: {}", id, email);
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with ID: " + id));

        if (!match.getUser1().getEmail().equals(email) && !match.getUser2().getEmail().equals(email)) {
            log.warn("Unauthorized attempt to access match ID: {} by user: {}", id, email);
            throw new UnauthorizedException("You are not authorized to view this match");
        }

        return mapToResponseDTO(match);
    }

    @Transactional
    public void refreshMatches() {
        log.info("Refreshing all user matches");
        // 1. Delete all existing matches
        matchRepository.deleteAll();

        // 2. Fetch all users
        List<User> users = userRepository.findAll();
        if (users.size() < 2) {
            log.info("Not enough users to perform matching");
            return;
        }

        // 3. Fetch and group all UserSkills by User
        List<UserSkill> allUserSkills = userSkillRepository.findAll();
        Map<User, List<UserSkill>> userSkillsMap = allUserSkills.stream()
                .collect(Collectors.groupingBy(UserSkill::getUser));

        List<Match> newMatches = new ArrayList<>();

        // 4. Pairwise compare all users
        for (int i = 0; i < users.size(); i++) {
            User u1 = users.get(i);
            List<UserSkill> u1Skills = userSkillsMap.getOrDefault(u1, List.of());

            Set<Long> u1TeachSkillIds = u1Skills.stream()
                    .filter(us -> us.getType() == SkillType.TEACH)
                    .map(us -> us.getSkill().getId())
                    .collect(Collectors.toSet());

            Set<Long> u1LearnSkillIds = u1Skills.stream()
                    .filter(us -> us.getType() == SkillType.LEARN)
                    .map(us -> us.getSkill().getId())
                    .collect(Collectors.toSet());

            for (int j = i + 1; j < users.size(); j++) {
                User u2 = users.get(j);
                List<UserSkill> u2Skills = userSkillsMap.getOrDefault(u2, List.of());

                Set<Long> u2TeachSkillIds = u2Skills.stream()
                        .filter(us -> us.getType() == SkillType.TEACH)
                        .map(us -> us.getSkill().getId())
                        .collect(Collectors.toSet());

                Set<Long> u2LearnSkillIds = u2Skills.stream()
                        .filter(us -> us.getType() == SkillType.LEARN)
                        .map(us -> us.getSkill().getId())
                        .collect(Collectors.toSet());

                // Intersections
                // How many of U1's TEACH skills does U2 want to LEARN?
                Set<Long> match12 = u1TeachSkillIds.stream()
                        .filter(u2LearnSkillIds::contains)
                        .collect(Collectors.toSet());

                // How many of U2's TEACH skills does U1 want to LEARN?
                Set<Long> match21 = u2TeachSkillIds.stream()
                        .filter(u1LearnSkillIds::contains)
                        .collect(Collectors.toSet());

                int c12 = match12.size();
                int c21 = match21.size();

                if (c12 > 0 || c21 > 0) {
                    int score;
                    if (c12 > 0 && c21 > 0) {
                        // Reciprocal
                        if (c12 == 1 && c21 == 1) {
                            score = 100; // Perfect reciprocal match
                        } else {
                            score = 75; // Multiple matching skills (reciprocal)
                        }
                    } else {
                        // One-sided
                        if (c12 == 1 || c21 == 1) {
                            score = 50; // One-sided match
                        } else {
                            score = 75; // Multiple matching skills (one-sided)
                        }
                    }

                    // Enforce u1.id < u2.id in saved Match entity to prevent duplicates
                    User first = u1.getId() < u2.getId() ? u1 : u2;
                    User second = u1.getId() < u2.getId() ? u2 : u1;

                    Match match = Match.builder()
                            .user1(first)
                            .user2(second)
                            .matchScore(score)
                            .createdAt(LocalDateTime.now())
                            .build();

                    newMatches.add(match);
                    log.info("Match generated: user1_id={}, user2_id={}, score={}%", first.getId(), second.getId(),
                            score);
                }
            }
        }

        if (!newMatches.isEmpty()) {
            matchRepository.saveAll(newMatches);
            log.info("Successfully generated and saved {} new matches", newMatches.size());
        }
    }

    private MatchResponseDTO mapToResponseDTO(Match match) {
        return MatchResponseDTO.builder()
                .id(match.getId())
                .user1(mapToUserResponse(match.getUser1()))
                .user2(mapToUserResponse(match.getUser2()))
                .matchScore(match.getMatchScore())
                .createdAt(match.getCreatedAt())
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
}
