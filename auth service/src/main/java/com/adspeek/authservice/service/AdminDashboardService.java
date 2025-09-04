package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.AdminDashboardStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminDashboardService {
    AdminDashboardStatsDTO getDashboardStats();
    Page<AdminDashboardStatsDTO.UpcomingDeadline> getUpcomingDeadlines(Pageable pageable);
    Long getCompletedFilingsCount();
}