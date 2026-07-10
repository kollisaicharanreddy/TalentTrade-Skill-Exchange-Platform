package com.talenttrade.service;

import com.talenttrade.dto.MatchResponseDTO;
import com.talenttrade.entity.*;
import com.talenttrade.exception.UnauthorizedException;
import com.talenttrade.repository.MatchRepository;
import com.talenttrade.repository.UserRepository;
import com.talenttrade.repository.UserSkillRepository;
import com.talenttrade.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @InjectMocks
    private MatchService matchService;

    @Test
    @DisplayName("Test: Get matches for a user\n" +
                 "Why: Verifies fetching paginated list of matches involving the user email.\n" +
                 "Expected: Returns a page of MatchResponseDTO.")
    void getMatchesForUser_success() {
        User u1 = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);
        User u2 = TestDataFactory.createUser(2L, "jane@example.com", "janedoe", Role.USER);
        Match match = Match.builder().id(1L).user1(u1).user2(u2).matchScore(100).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(matchRepository.findByUser1EmailOrUser2Email("john@example.com", "john@example.com", pageable))
                .thenReturn(new PageImpl<>(List.of(match)));

        Page<MatchResponseDTO> result = matchService.getMatchesForUser("john@example.com", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(100, result.getContent().get(0).getMatchScore());
    }

    @Test
    @DisplayName("Test: Get match by ID success for authorized user\n" +
                 "Why: Ensures a user can fetch details of a match they are part of.\n" +
                 "Expected: Returns the match details.")
    void getMatchById_authorized() {
        User u1 = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);
        User u2 = TestDataFactory.createUser(2L, "jane@example.com", "janedoe", Role.USER);
        Match match = Match.builder().id(1L).user1(u1).user2(u2).matchScore(75).build();

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        MatchResponseDTO response = matchService.getMatchById(1L, "john@example.com");

        assertNotNull(response);
        assertEquals(75, response.getMatchScore());
    }

    @Test
    @DisplayName("Test: Get match by ID throws UnauthorizedException for non-participant\n" +
                 "Why: Enforces security boundaries preventing third-party users from snooping on user matches.\n" +
                 "Expected: UnauthorizedException is thrown.")
    void getMatchById_unauthorized() {
        User u1 = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);
        User u2 = TestDataFactory.createUser(2L, "jane@example.com", "janedoe", Role.USER);
        Match match = Match.builder().id(1L).user1(u1).user2(u2).matchScore(75).build();

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(UnauthorizedException.class, () -> matchService.getMatchById(1L, "spy@example.com"));
    }

    @Test
    @DisplayName("Test: Refresh matches algorithm logic\n" +
                 "Why: Verifies the engine correctly matches users based on their skills (reciprocal teach-learn intersections) and saves them with correct ID ordering.\n" +
                 "Expected: Correct score calculation (100 for perfect reciprocal matching) and matches saved.")
    @SuppressWarnings("unchecked")
    void refreshMatches_success() {
        User u1 = TestDataFactory.createUser(1L, "john@example.com", "johndoe", Role.USER);
        User u2 = TestDataFactory.createUser(2L, "jane@example.com", "janedoe", Role.USER);

        Skill s1 = TestDataFactory.createSkill(1L, "Java", "Programming");
        Skill s2 = TestDataFactory.createSkill(2L, "Python", "Programming");

        // John wants to TEACH Java, LEARN Python
        UserSkill us1Teach = TestDataFactory.createUserSkill(1L, u1, s1, SkillType.TEACH, SkillLevel.EXPERT);
        UserSkill us1Learn = TestDataFactory.createUserSkill(2L, u1, s2, SkillType.LEARN, SkillLevel.BEGINNER);

        // Jane wants to LEARN Java, TEACH Python
        UserSkill us2Learn = TestDataFactory.createUserSkill(3L, u2, s1, SkillType.LEARN, SkillLevel.INTERMEDIATE);
        UserSkill us2Teach = TestDataFactory.createUserSkill(4L, u2, s2, SkillType.TEACH, SkillLevel.EXPERT);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));
        when(userSkillRepository.findAll()).thenReturn(List.of(us1Teach, us1Learn, us2Learn, us2Teach));

        matchService.refreshMatches();

        verify(matchRepository).deleteAll();
        
        ArgumentCaptor<List<Match>> matchesCaptor = ArgumentCaptor.forClass(List.class);
        verify(matchRepository).saveAll(matchesCaptor.capture());

        List<Match> savedMatches = matchesCaptor.getValue();
        assertEquals(1, savedMatches.size());
        
        Match match = savedMatches.get(0);
        // ID order: u1.id (1L) < u2.id (2L) -> user1 must be John, user2 must be Jane
        assertEquals(u1.getId(), match.getUser1().getId());
        assertEquals(u2.getId(), match.getUser2().getId());
        assertEquals(100, match.getMatchScore()); // Perfect reciprocal match
    }
}
