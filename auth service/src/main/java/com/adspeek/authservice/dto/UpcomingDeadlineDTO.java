package com.adspeek.authservice.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingDeadlineDTO {
    private Long taskId;
    private String taskName;
    private String description;
    private LocalDate dueDate;
    private String status;
    private String priority;
    private String taskType;
    private Long staffId;
    private String staffName;
    private String latestRemark;
    private String deadlineStatus; // Overdue, Due Soon, Safe
    private Boolean hasDocuments;
}