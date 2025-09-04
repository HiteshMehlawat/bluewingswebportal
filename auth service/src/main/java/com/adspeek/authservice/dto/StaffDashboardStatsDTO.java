package com.adspeek.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDashboardStatsDTO {
    private Long totalAssignedTasks;
    private Long pendingTasks;
    private Long inProgressTasks;
    private Long completedTasks;
    private Long onHoldTasks;
    private Long cancelledTasks;
    private Long overdueTasks;
    private Long totalAssignedClients;
    private Double averageTaskCompletionTime;
}
