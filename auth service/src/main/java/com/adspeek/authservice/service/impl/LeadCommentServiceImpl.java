package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.LeadCommentDTO;
import com.adspeek.authservice.entity.LeadComment;
import com.adspeek.authservice.entity.Lead;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.repository.LeadCommentRepository;
import com.adspeek.authservice.repository.LeadRepository;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.service.LeadCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeadCommentServiceImpl implements LeadCommentService {

    private final LeadCommentRepository leadCommentRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    @Override
    public LeadCommentDTO createComment(LeadCommentDTO commentDTO) {
        Lead lead = leadRepository.findById(commentDTO.getLeadId())
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        User user = getCurrentUser();

        LeadComment comment = LeadComment.builder()
                .lead(lead)
                .user(user)
                .comment(commentDTO.getComment())
                .commentType(commentDTO.getCommentType() != null ? commentDTO.getCommentType()
                        : LeadComment.CommentType.NOTE)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        LeadComment savedComment = leadCommentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    @Override
    public LeadCommentDTO getCommentById(Long id) {
        LeadComment comment = leadCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        return convertToDTO(comment);
    }

    @Override
    public List<LeadCommentDTO> getCommentsByLeadId(Long leadId) {
        return leadCommentRepository.findByLeadIdOrderByCreatedAtDesc(leadId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<LeadCommentDTO> getCommentsByLeadId(Long leadId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leadCommentRepository.findByLeadIdOrderByCreatedAtDesc(leadId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public LeadCommentDTO updateComment(Long id, LeadCommentDTO commentDTO) {
        LeadComment comment = leadCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setComment(commentDTO.getComment());
        if (commentDTO.getCommentType() != null) {
            comment.setCommentType(commentDTO.getCommentType());
        }

        LeadComment savedComment = leadCommentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    @Override
    public void deleteComment(Long id) {
        LeadComment comment = leadCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        leadCommentRepository.delete(comment);
    }

    private LeadCommentDTO convertToDTO(LeadComment comment) {
        return LeadCommentDTO.builder()
                .id(comment.getId())
                .leadId(comment.getLead().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .comment(comment.getComment())
                .commentType(comment.getCommentType())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}
