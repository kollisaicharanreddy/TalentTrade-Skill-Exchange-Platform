package com.talenttrade.integration;

import com.talenttrade.BaseIntegrationTest;
import com.talenttrade.entity.*;
import com.talenttrade.repository.MatchRepository;
import com.talenttrade.repository.SkillRepository;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.repository.UserSkillRepository;
import com.talenttrade.service.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    @DisplayName("Workflow 3 Integration: Generate Match -> Verify Match Stored")
    void generateMatchWorkflow() {
        // 1. Create Users
        User u1 = User.builder()
                .fullName("Matcher One")
                .email("m1@example.com")
                .username("matcher1")
                .password("pass")
                .role(Role.USER)
                .emailVerified(true)
                .enabled(true)
                .build();
        u1 = userRepository.save(u1);

        User u2 = User.builder()
                .fullName("Matcher Two")
                .email("m2@example.com")
                .username("matcher2")
                .password("pass")
                .role(Role.USER)
                .emailVerified(true)
                .enabled(true)
                .build();
        u2 = userRepository.save(u2);

        // 2. Create Skills
        Skill s1 = Skill.builder().name("Java Integration Match Skill").category("IT").build();
        s1 = skillRepository.save(s1);

        // 3. Setup User Skills (Reciprocal match setup)
        // User 1 teaches Java
        UserSkill us1 = UserSkill.builder().user(u1).skill(s1).type(SkillType.TEACH).level(SkillLevel.EXPERT).build();
        userSkillRepository.save(us1);

        // User 2 wants to learn Java
        UserSkill us2 = UserSkill.builder().user(u2).skill(s1).type(SkillType.LEARN).level(SkillLevel.BEGINNER).build();
        userSkillRepository.save(us2);

        // 4. Generate Match
        matchService.refreshMatches();

        // 5. Verify Match Stored
        List<Match> matches = matchRepository.findAll();
        assertFalse(matches.isEmpty());
        
        Match match = matches.get(0);
        assertEquals(50, match.getMatchScore()); // One-sided match = 50%
        assertTrue((match.getUser1().getId().equals(u1.getId()) && match.getUser2().getId().equals(u2.getId())) ||
                   (match.getUser1().getId().equals(u2.getId()) && match.getUser2().getId().equals(u1.getId())));
    }
}
