package com.adspeek.authservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffActivityDTO {
    private Long id;
    private Long staffId;
    private String staffName;
    private String activityType;
    private String taskDescription;
    private String workStatus;
    private LocalDate logDate;
    private LocalTime loginTime;
    private LocalTime logoutTime;
    private Integer durationMinutes;
    private Long clientId;
    private String clientName;
    private Long taskId;
    private String taskTitle;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}