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
public class ReviewResponseDTO {
    private Long id;
    private Long sessionId;
    private UserResponse reviewer;
    private UserResponse reviewee;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
