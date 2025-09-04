package com.adspeek.authservice.dto;

import com.adspeek.authservice.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private Notification.NotificationType notificationType;
    private Boolean isRead;
    private Long relatedTaskId;
    private Long relatedDocumentId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    // Additional fields for better UI display
    private String taskTitle;
    private String documentName;
    private String senderName;
    private String senderRole;
    
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType())
                .isRead(notification.getIsRead())
                .relatedTaskId(notification.getRelatedTask() != null ? notification.getRelatedTask().getId() : null)
                .relatedDocumentId(notification.getRelatedDocument() != null ? notification.getRelatedDocument().getId() : null)
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .taskTitle(notification.getRelatedTask() != null ? notification.getRelatedTask().getTitle() : null)
                .documentName(notification.getRelatedDocument() != null ? notification.getRelatedDocument().getOriginalFileName() : null)
                .build();
    }
}
