package com.adspeek.authservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffPerformanceSummaryDTO {
    private String employeeId;
    private String name;
    private String position;
    private String department;
    private Long supervisorId;
    private Long totalAssigned;
    private Long completed;
    private Long pending;
    private Long overdue;
    private Long totalAssignedClients;
    private LocalDateTime lastActivity;
}