package com.adspeek.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lead_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CommentType commentType = CommentType.NOTE;

    private LocalDateTime createdAt;

    public enum CommentType {
        NOTE, CALL, EMAIL, MEETING, PROPOSAL, FOLLOW_UP
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
