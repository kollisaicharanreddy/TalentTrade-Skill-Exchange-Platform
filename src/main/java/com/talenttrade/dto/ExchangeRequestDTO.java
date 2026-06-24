package com.talenttrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequestDTO {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotBlank(message = "Request message is required")
    private String message;
}
