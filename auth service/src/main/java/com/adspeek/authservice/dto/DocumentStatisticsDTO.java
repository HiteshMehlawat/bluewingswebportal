package com.adspeek.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatisticsDTO {
    private Long totalDocuments;
    private Long verifiedDocuments;
    private Long pendingDocuments;
    private Long rejectedDocuments;
    private Long documentsUploadedToday;
    private Long documentsUploadedThisWeek;
    private Long documentsUploadedThisMonth;
    private Long totalFileSize;
    private Long averageFileSize;
}