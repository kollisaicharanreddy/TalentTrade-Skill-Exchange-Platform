package com.talenttrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDTO {
    private long skillsOffered;
    private long skillsWanted;
    private long matches;
    private long requestsSent;
    private long requestsReceived;
    private long acceptedRequests;
    private long rejectedRequests;
    private long upcomingSessions;
    private long completedSessions;
    private double averageRating;
    private long totalReviews;
}
