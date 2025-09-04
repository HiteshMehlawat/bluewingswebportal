package com.adspeek.authservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestDetailDTO extends ServiceRequestDTO {
    // Additional client details
    private String clientAddress;
    private String clientCity;
    private String clientState;
    private String clientPincode;
    private String clientGstNumber;
    private String clientPanNumber;

    // Additional staff details
    private String assignedStaffPhone;
    private String assignedStaffPosition;
    private String assignedStaffDepartment;

    // Additional user email details
    private String acceptedByEmail;
    private String assignedByEmail;
    private String rejectedByEmail;
    private String createdByEmail;
    private String updatedByEmail;

    // UI badge classes
    private String statusBadgeClass;
    private String priorityBadgeClass;
}
