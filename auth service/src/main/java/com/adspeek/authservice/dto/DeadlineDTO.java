package com.adspeek.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineDTO {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private String taskDescription;
    private LocalDateTime dueDate;
    private String status; // PENDING, IN_PROGRESS, COMPLETED, OVERDUE
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String taskType;
    private Long clientId;
    private String clientName;
    private Long assignedStaffId;
    private String assignedStaffName;
    private Integer daysRemaining;
    private Boolean isOverdue;
    private String deadlineStatus; // SAFE, DUE_SOON, OVERDUE
    private Boolean hasDocuments;
    private String latestComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
