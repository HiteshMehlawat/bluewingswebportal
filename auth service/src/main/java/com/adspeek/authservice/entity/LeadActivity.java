package com.adspeek.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lead_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType activityType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String oldValue;

    @Column(length = 255)
    private String newValue;

    private LocalDateTime createdAt;

    public enum ActivityType {
        LEAD_CREATED, STATUS_CHANGED, ASSIGNED, CONTACTED, FOLLOW_UP, PROPOSAL_SENT, CONVERTED, LOST
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
