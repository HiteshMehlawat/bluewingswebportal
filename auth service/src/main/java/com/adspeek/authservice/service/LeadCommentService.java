package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.LeadCommentDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LeadCommentService {

    LeadCommentDTO createComment(LeadCommentDTO commentDTO);

    LeadCommentDTO getCommentById(Long id);

    List<LeadCommentDTO> getCommentsByLeadId(Long leadId);

    Page<LeadCommentDTO> getCommentsByLeadId(Long leadId, int page, int size);

    LeadCommentDTO updateComment(Long id, LeadCommentDTO commentDTO);

    void deleteComment(Long id);
}
