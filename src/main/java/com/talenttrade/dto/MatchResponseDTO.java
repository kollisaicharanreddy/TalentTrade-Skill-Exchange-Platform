package com.talenttrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponseDTO {
    private Long id;
    private UserResponse user1;
    private UserResponse user2;
    private Integer matchScore;
    private LocalDateTime createdAt;
}
