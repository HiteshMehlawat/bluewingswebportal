package com.adspeek.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineStatisticsDTO {
    private Long totalDeadlines;
    private Long pendingDeadlines;
    private Long inProgressDeadlines;
    private Long completedDeadlines;
    private Long overdueDeadlines;
    private Long dueSoonDeadlines;
    private Long safeDeadlines;
}
