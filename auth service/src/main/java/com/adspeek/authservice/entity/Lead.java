package com.adspeek.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String leadId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 255)
    private String companyName;

    @Column(name = "service_item_id")
    private Long serviceItemId;

    @Column(name = "service_category_name", length = 255)
    private String serviceCategoryName;

    @Column(name = "service_subcategory_name", length = 255)
    private String serviceSubcategoryName;

    @Column(name = "service_item_name", length = 255)
    private String serviceItemName;

    @Column(columnDefinition = "TEXT")
    private String serviceDescription;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Source source = Source.WEBSITE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.NEW;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority = Priority.MEDIUM;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private Staff assignedStaff;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedValue;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDate nextFollowUpDate;
    private LocalDate lastContactDate;
    private LocalDateTime convertedDate;

    @Column(columnDefinition = "TEXT")
    private String lostReason;

    @Column(length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    public enum Source {
        WEBSITE, SOCIAL_MEDIA, REFERRAL, COLD_CALL, ADVERTISING, OTHER
    }

    public enum Status {
        NEW, CONTACTED, IN_DISCUSSION, PROPOSAL_SENT, CONVERTED, LOST
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Note: leadId will be set by the service layer using IdGenerationService
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
