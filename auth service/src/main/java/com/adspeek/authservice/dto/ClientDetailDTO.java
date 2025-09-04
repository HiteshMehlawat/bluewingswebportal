package com.adspeek.authservice.dto;

import com.adspeek.authservice.entity.Client;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDetailDTO {
    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String companyName;
    private String companyType;
    private String gstNumber;
    private String panNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String businessType;
    private String industry;
    private String website;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String emergencyContact;
    private Client.ClientType clientType;
    private LocalDate registrationDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Assigned Staff Information
    private Long assignedStaffId;
    private String assignedStaffName;
    private String assignedStaffEmail;
    private String assignedStaffPhone;

    // Audit Information
    private String createdBy;
    private Long createdById;
    private String updatedBy;
    private Long updatedById;

    // Statistics
    private Long totalDocuments;
    private Long totalTasks;
    private Long completedTasks;
    private Long pendingTasks;
    private Long overdueTasks;
}