package com.adspeek.authservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceItemDTO {
    private Long id;
    private String name;
    private String description;
    private Integer estimatedHours;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long subcategoryId;
    private String subcategoryName;
    private Long categoryId;
    private String categoryName;
}
