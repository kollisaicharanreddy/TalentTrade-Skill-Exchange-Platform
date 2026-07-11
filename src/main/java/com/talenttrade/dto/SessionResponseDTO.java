package com.talenttrade.dto;

import com.talenttrade.entity.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponseDTO {
    private Long id;
    private Long exchangeRequestId;
    private UserResponse mentor;
    private UserResponse learner;
    private LocalDate scheduledDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String meetingLink;
    private SessionStatus status;
    private String notes;
    private String googleEventId;
    private String calendarProvider;
    private String meetingStatus;
    private LocalDateTime lastSynced;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
