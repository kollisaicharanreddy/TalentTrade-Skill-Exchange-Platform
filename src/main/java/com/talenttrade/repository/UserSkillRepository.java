package com.talenttrade.repository;

import com.talenttrade.entity.Skill;
import com.talenttrade.entity.SkillType;
import com.talenttrade.entity.User;
import com.talenttrade.entity.UserSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUser(User user);
    List<UserSkill> findByUserEmail(String email);
    boolean existsByUserAndSkillAndType(User user, Skill skill, SkillType type);
    Page<UserSkill> findBySkillNameContainingIgnoreCaseAndType(String skillName, SkillType type, Pageable pageable);
}
