package com.adspeek.authservice.dto;

import com.adspeek.authservice.entity.LeadComment;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadCommentDTO {
    private Long id;
    private Long leadId;
    private Long userId;
    private String userName;
    private String comment;
    private LeadComment.CommentType commentType;
    private LocalDateTime createdAt;
}
