package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.LeadComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadCommentRepository extends JpaRepository<LeadComment, Long> {

    List<LeadComment> findByLeadIdOrderByCreatedAtDesc(Long leadId);

    Page<LeadComment> findByLeadIdOrderByCreatedAtDesc(Long leadId, Pageable pageable);

    long countByLeadId(Long leadId);
}
