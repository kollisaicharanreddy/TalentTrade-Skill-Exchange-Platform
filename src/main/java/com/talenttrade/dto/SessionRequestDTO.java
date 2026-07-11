package com.talenttrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionRequestDTO {

    @NotNull(message = "Exchange request ID is required")
    private Long exchangeRequestId;

    @NotNull(message = "Mentor ID is required")
    private Long mentorId;

    @NotNull(message = "Learner ID is required")
    private Long learnerId;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String meetingLink;

    private String notes;
}
