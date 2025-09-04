package com.adspeek.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_task_id")
    private Task relatedTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_document_id")
    private Document relatedDocument;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public enum NotificationType {
        TASK_ASSIGNED,
        STATUS_UPDATE,
        DOCUMENT_UPLOADED,
        DOCUMENT_VERIFIED,
        DOCUMENT_REJECTED,
        DEADLINE_REMINDER,
        STAFF_ASSIGNED,
        TASK_COMPLETED,
        TASK_ACKNOWLEDGED,
        SYSTEM,
        EMAIL,
        MESSAGE_RECEIVED,
        LEAD_ASSIGNED,
        NEW_LEAD_CREATED,
        LEAD_CONVERTED,
        // Service Request notification types
        SERVICE_REQUEST_CREATED,
        SERVICE_REQUEST_STATUS_CHANGED,
        SERVICE_REQUEST_ASSIGNED,
        SERVICE_REQUEST_REJECTED,
        SERVICE_REQUEST_CANCELLED,
        SERVICE_REQUEST_CONVERTED_TO_TASK
    }
}
