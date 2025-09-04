package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.LeadActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadActivityRepository extends JpaRepository<LeadActivity, Long> {

    List<LeadActivity> findByLeadIdOrderByCreatedAtDesc(Long leadId);

    Page<LeadActivity> findByLeadIdOrderByCreatedAtDesc(Long leadId, Pageable pageable);

    long countByLeadId(Long leadId);
}
