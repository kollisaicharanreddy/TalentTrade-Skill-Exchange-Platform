package com.talenttrade.dto;

import com.talenttrade.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequestResponseDTO {
    private Long id;
    private UserResponse sender;
    private UserResponse receiver;
    private String message;
    private RequestStatus status;
    private LocalDateTime createdAt;
}
