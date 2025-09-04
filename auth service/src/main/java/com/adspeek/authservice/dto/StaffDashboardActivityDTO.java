package com.adspeek.authservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDashboardActivityDTO {
    private Long id;
    private String activityType;
    private String title;
    private String description;
    private String taskTitle;
    private String clientName;
    private String workStatus;
    private LocalDateTime timestamp;
    private String icon;
    private String color;
}
