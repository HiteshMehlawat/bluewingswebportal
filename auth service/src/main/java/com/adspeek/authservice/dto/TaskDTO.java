package com.adspeek.authservice.dto;

import com.adspeek.authservice.entity.Task;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Long clientId;
    private String clientName;
    private Long assignedStaffId;
    private String assignedStaffName;
    private String assignedStaffEmployeeId;
    private Task.TaskType taskType;
    private Task.Status status;
    private Task.Priority priority;
    private LocalDate dueDate;
    private LocalDateTime assignedDate;
    private LocalDateTime startedDate;
    private LocalDateTime completedDate;
    private Double estimatedHours;
    private Double actualHours;
    private Long createdBy;
    private String createdByName;
    private String createdByEmail;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String deadlineStatus; // Overdue, Due Soon, Safe
    private Boolean hasDocuments;
    private String latestComment;
    // Service information
    private Long serviceItemId;
}