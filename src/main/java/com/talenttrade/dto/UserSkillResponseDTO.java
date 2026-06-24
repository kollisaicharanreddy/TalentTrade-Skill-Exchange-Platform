package com.talenttrade.dto;

import com.talenttrade.entity.SkillLevel;
import com.talenttrade.entity.SkillType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkillResponseDTO {
    private Long id;
    private UserResponse user;
    private SkillResponseDTO skill;
    private SkillType type;
    private SkillLevel level;
}
