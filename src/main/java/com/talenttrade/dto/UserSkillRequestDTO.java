package com.talenttrade.dto;

import com.talenttrade.entity.SkillLevel;
import com.talenttrade.entity.SkillType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkillRequestDTO {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Skill type (TEACH/LEARN) is required")
    private SkillType type;

    @NotNull(message = "Skill level is required")
    private SkillLevel level;
}
