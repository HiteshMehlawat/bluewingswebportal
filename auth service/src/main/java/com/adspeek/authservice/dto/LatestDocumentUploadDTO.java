package com.adspeek.authservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LatestDocumentUploadDTO {
    private Long documentId;
    private String documentName;
    private String originalFileName;
    private LocalDateTime uploadDateTime;
    private String fileType;
    private Long fileSize;
    private String documentType;
    private Boolean isVerified;
    private String status; // For review/approval status if you add more states
    private Long uploadedById;
    private String uploadedByName;
    private String uploadedByRole;
    private Long clientId;
    private String clientName;
    private String taskName;
    private Long verifiedById;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private Long rejectedById;
    private String rejectedByName;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    // Optionally, add downloadUrl if you want to build it in backend
}