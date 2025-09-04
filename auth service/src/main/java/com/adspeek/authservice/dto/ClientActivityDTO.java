package com.adspeek.authservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientActivityDTO {
    private Long id;
    private String activityType; // DOCUMENT_UPLOAD, TASK_UPDATE, LOGIN, PROFILE_UPDATE, etc.
    private String description;
    private String details;
    private LocalDateTime activityDate;
    private String performedBy; // Client name or staff name
    private String performedByRole; // CLIENT, STAFF, ADMIN
    private String relatedEntity; // Document name, task name, etc.
    private Long relatedEntityId;
    private String ipAddress;
    private String userAgent;
}