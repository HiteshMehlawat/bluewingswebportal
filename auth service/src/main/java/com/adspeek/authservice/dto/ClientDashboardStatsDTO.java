package com.adspeek.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardStatsDTO {
    private Long totalTasks;
    private Long pendingTasks;
    private Long completedTasks;
    private Long overdueTasks;
    private Long pendingDocuments;
    private Long verifiedDocuments;
    private Long upcomingDeadlines;
}
