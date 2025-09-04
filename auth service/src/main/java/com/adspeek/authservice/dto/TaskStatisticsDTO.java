package com.adspeek.authservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatisticsDTO {
    private Long totalTasks;
    private Long pendingTasks;
    private Long inProgressTasks;
    private Long completedTasks;
    private Long onHoldTasks;
    private Long cancelledTasks;
    private Long overdueTasks;
}