package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.AdminDashboardStatsDTO;
import com.adspeek.authservice.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard-stats")
    public AdminDashboardStatsDTO getDashboardStats() {
        return adminDashboardService.getDashboardStats();
    }

    @GetMapping("/upcoming-deadlines")
    public Page<AdminDashboardStatsDTO.UpcomingDeadline> getUpcomingDeadlines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminDashboardService.getUpcomingDeadlines(PageRequest.of(page, size));
    }

    @GetMapping("/completed-filings-count")
    public Long getCompletedFilingsCount() {
        return adminDashboardService.getCompletedFilingsCount();
    }
}