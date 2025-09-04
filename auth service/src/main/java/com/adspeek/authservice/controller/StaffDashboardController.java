package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.StaffDashboardStatsDTO;
import com.adspeek.authservice.service.StaffDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/staff-dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class StaffDashboardController {

    private final StaffDashboardService staffDashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<StaffDashboardStatsDTO> getDashboardStats() {
        try {
            StaffDashboardStatsDTO stats = staffDashboardService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching staff dashboard stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getRecentTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        try {
            var tasks = staffDashboardService.getRecentTasks(page, size);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error fetching staff recent tasks: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/activities")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getRecentActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            var activities = staffDashboardService.getRecentActivities(page, size);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            log.error("Error fetching staff recent activities: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
