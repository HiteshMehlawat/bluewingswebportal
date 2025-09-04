package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.DeadlineDTO;
import com.adspeek.authservice.dto.DeadlineStatisticsDTO;
import com.adspeek.authservice.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeadlineService {
        Page<DeadlineDTO> getDeadlines(Long clientId, String status, String priority, String taskType,
                        Boolean isOverdue, String searchTerm, Pageable pageable);

        Page<DeadlineDTO> getClientDeadlines(Long clientId, String status, String priority, String taskType,
                        Boolean isOverdue, String searchTerm, Pageable pageable);

        List<DeadlineDTO> getOverdueDeadlines();

        List<DeadlineDTO> getDueSoonDeadlines();

        DeadlineStatisticsDTO getDeadlineStatistics();

        DeadlineDTO getDeadlineById(Long id);

        DeadlineDTO updateDeadlineStatus(Long deadlineId, String status);

        DeadlineDTO extendDeadline(Long deadlineId, String newDueDate);

        List<DeadlineDTO> getDeadlinesByDateRange(String startDate, String endDate);

        List<DeadlineDTO> getUpcomingDeadlines(int limit);

        void sendDeadlineReminder(Long deadlineId);
}
