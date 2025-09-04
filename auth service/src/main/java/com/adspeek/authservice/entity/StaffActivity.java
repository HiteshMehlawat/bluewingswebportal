package com.adspeek.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(columnDefinition = "TEXT")
    private String taskDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status")
    private WorkStatus workStatus = WorkStatus.PENDING;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(name = "login_time")
    private LocalTime loginTime;

    @Column(name = "logout_time")
    private LocalTime logoutTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 0;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    public enum ActivityType {
        LOGIN, LOGOUT, TASK_STARTED, TASK_COMPLETED, TASK_DELAYED, TASK_CANCELLED, TASK_PENDING,
        CLIENT_ASSIGNED, DOCUMENT_UPLOADED, DOCUMENT_VERIFIED, DOCUMENT_REJECTED, CLIENT_CONTACT, BREAK_START, BREAK_END
    }

    public enum WorkStatus {
        PENDING, IN_PROGRESS, COMPLETED, DELAYED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}