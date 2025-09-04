package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.StaffActivityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StaffActivityService {

    // CRUD operations
    StaffActivityDTO createActivity(StaffActivityDTO activityDTO);

    StaffActivityDTO updateActivity(Long id, StaffActivityDTO activityDTO);

    void deleteActivity(Long id);

    StaffActivityDTO getActivityById(Long id);

    // Query operations
    Page<StaffActivityDTO> getActivitiesByStaff(Long staffId, Pageable pageable);

    Page<StaffActivityDTO> getActivitiesByDate(LocalDate date, Pageable pageable);

    Page<StaffActivityDTO> getActivitiesByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<StaffActivityDTO> getActivitiesByStaffAndDateRange(Long staffId, LocalDate startDate, LocalDate endDate,
            Pageable pageable);

    // Login/Logout tracking
    StaffActivityDTO logLogin(Long staffId);

    StaffActivityDTO logLogout(Long staffId);

    // Task status tracking
    StaffActivityDTO startTask(Long staffId, String taskDescription, Long taskId, Long clientId);

    StaffActivityDTO completeTask(Long staffId, String taskDescription, Long taskId, Long clientId);

    StaffActivityDTO delayTask(Long staffId, String taskDescription, Long taskId, Long clientId, String reason);

    // Reports and summaries
    Map<String, Object> getDailySummary(Long staffId, LocalDate date);

    List<Map<String, Object>> getWeeklySummary(Long staffId, LocalDate startDate, LocalDate endDate);

    Map<String, Object> getWorkloadSummary(LocalDate date);

    Map<String, Object> getStaffPerformance(Long staffId, LocalDate startDate, LocalDate endDate);
}