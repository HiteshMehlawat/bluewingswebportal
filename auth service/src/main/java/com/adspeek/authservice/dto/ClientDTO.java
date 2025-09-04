package com.adspeek.authservice.dto;

import com.adspeek.authservice.entity.Client;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {
    private Long id;
    private Long userId;

    // User information
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;

    // Client specific information
    private String companyName;
    private String companyType;
    private String panNumber;
    private String gstNumber;
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

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for management
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer pendingTasks;
    private String assignedStaffName;
    private Long assignedStaffId;
}