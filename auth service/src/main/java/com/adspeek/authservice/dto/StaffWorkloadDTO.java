package com.adspeek.authservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffWorkloadDTO {
    private Long staffId;
    private String staffName;
    private String employeeId;
    private String position;
    private String department;
    private Long totalTasks;
    private Long pendingTasks;
    private Long inProgressTasks;
    private Long completedTasks;
    private Long overdueTasks;
    private Long currentTasks;
}