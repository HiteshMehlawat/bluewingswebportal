package com.adspeek.authservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDTO {
    private Long id;
    private Long userId;
    private String employeeId;
    private String position;
    private String department;
    private LocalDate joiningDate;
    private Double salary;
    private Long supervisorId;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User information for dropdown display
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role; // ADMIN, STAFF

    // Audit fields
    private String createdBy;
    private Long createdById;
    private String updatedBy;
    private Long updatedById;

    // Supervisor information
    private String supervisorName;
    private String supervisorEmail;
    private String supervisorPhone;
    private String supervisorEmployeeId;
}