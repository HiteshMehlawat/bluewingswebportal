package com.adspeek.authservice.dto;

import com.adspeek.authservice.entity.Lead;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadDTO {
    private Long id;
    private String leadId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String companyName;
    private Long serviceItemId;
    private String serviceCategoryName;
    private String serviceSubcategoryName;
    private String serviceItemName;
    private String serviceDescription;
    private Lead.Source source;
    private Lead.Status status;
    private Lead.Priority priority;
    private Long assignedStaffId;
    private String assignedStaffName;
    private String assignedStaffEmail;
    private String assignedStaffPhone;
    private BigDecimal estimatedValue;
    private String notes;
    private LocalDate nextFollowUpDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastContactDate;

    private LocalDateTime convertedDate;
    private String lostReason;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private String createdByName;
    private Long updatedById;
    private String updatedByName;
}
