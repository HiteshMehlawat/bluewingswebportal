package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.StaffDashboardStatsDTO;
import com.adspeek.authservice.dto.StaffDashboardActivityDTO;
import com.adspeek.authservice.dto.TaskDTO;
import org.springframework.data.domain.Page;

public interface StaffDashboardService {
    StaffDashboardStatsDTO getDashboardStats();

    Page<TaskDTO> getRecentTasks(int page, int size);

    Page<StaffDashboardActivityDTO> getRecentActivities(int page, int size);
}
