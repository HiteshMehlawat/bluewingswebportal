package com.adspeek.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskConversionDTO {
    private String title;
    private String description;
    private String priority;
    private LocalDate dueDate;
    private BigDecimal estimatedHours;
    private Long assignedStaffId;
}
