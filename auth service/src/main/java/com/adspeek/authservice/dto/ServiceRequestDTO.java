package com.adspeek.authservice.dto;

import com.adspeek.authservice.entity.ServiceRequest;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestDTO {
    private Long id;
    private String requestId;
    private String serviceCategoryName;
    private String serviceSubcategoryName;
    private Long serviceItemId;
    private String serviceItemName;
    private String description;
    private String notes;
    private LocalDate preferredDeadline;
    private ServiceRequest.Priority priority;
    private ServiceRequest.Status status;
    private String rejectionReason;
    private String adminNotes;
    private String staffNotes;
    private BigDecimal estimatedPrice;
    private BigDecimal finalPrice;
    private LocalDateTime assignedDate;
    private LocalDateTime completedDate;
    private LocalDateTime rejectedDate;

    // Client information
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String companyName;

    // Staff information
    private Long assignedStaffId;
    private String assignedStaffName;
    private String assignedStaffEmail;
    private String assignedStaffEmployeeId;

    // User information for actions
    private Long acceptedById;
    private String acceptedByName;
    private Long assignedById;
    private String assignedByName;
    private Long rejectedById;
    private String rejectedByName;
    private Long createdById;
    private String createdByName;
    private Long updatedById;
    private String updatedByName;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
