package com.adspeek.authservice.dto;

import com.adspeek.authservice.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private Long taskId;
    private Long clientId;
    private String clientName;
    private Long uploadedById;
    private String uploadedByName;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Document.DocumentType documentType;
    private Document.DocumentStatus status;
    private Long verifiedById;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private Long rejectedById;
    private String rejectedByName;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private LocalDateTime uploadDate;
    private Long createdById;
    private String createdByName;
    private Long updatedById;
    private String updatedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}