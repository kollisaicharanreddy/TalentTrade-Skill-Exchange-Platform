package com.talenttrade.repository;

import com.talenttrade.entity.Match;
import com.talenttrade.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByUser1OrUser2(User user1, User user2);
    List<Match> findByUser1EmailOrUser2Email(String email1, String email2);
    Page<Match> findByUser1EmailOrUser2Email(String email1, String email2, Pageable pageable);
}
