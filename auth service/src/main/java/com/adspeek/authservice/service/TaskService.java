package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.TaskDTO;
import com.adspeek.authservice.dto.TaskDetailDTO;
import com.adspeek.authservice.dto.TaskStatisticsDTO;
import com.adspeek.authservice.dto.StaffWorkloadDTO;
import com.adspeek.authservice.entity.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.adspeek.authservice.dto.UpcomingDeadlineDTO;

public interface TaskService {
        TaskDTO createTask(TaskDTO taskDTO);

        TaskDTO updateTask(Long id, TaskDTO taskDTO);

        void deleteTask(Long id);

        Optional<TaskDTO> getTaskById(Long id);

        Optional<TaskDetailDTO> getTaskDetailById(Long id);

        List<TaskDTO> getAllTasks();

        Page<TaskDTO> getTasksWithFilters(
                        Task.Status status,
                        Task.Priority priority,
                        Long assignedStaffId,
                        Long clientId,
                        String searchTerm,
                        Long serviceCategoryId,
                        Long serviceSubcategoryId,
                        Long serviceItemId,
                        Pageable pageable);

        Page<TaskDTO> searchTasks(String searchTerm, Pageable pageable);

        List<TaskDTO> getOverdueTasks();

        List<TaskDTO> getTasksDueSoon();

        Page<TaskDTO> getTasksByStaff(Long staffId, Pageable pageable);

        Page<TaskDTO> getTasksByStaffAndClient(Long staffId, Long clientId, Pageable pageable);

        TaskStatisticsDTO getTaskStatistics();

        List<StaffWorkloadDTO> getStaffWorkloadSummary();

        List<StaffWorkloadDTO> getAvailableStaffForAssignment();

        TaskDTO reassignTask(Long taskId, Long newStaffId);

        TaskDTO updateTaskStatus(Long taskId, Task.Status newStatus);

        Page<UpcomingDeadlineDTO> getUpcomingDeadlinesByClient(Long clientId, Pageable pageable);

        // Client Dashboard specific methods
        Page<TaskDTO> getTasksByClient(Long clientId, Pageable pageable);

        // Client Dashboard detailed methods
        Page<TaskDetailDTO> getTasksByClientWithDetails(Long clientId, Pageable pageable);

        Page<TaskDetailDTO> getTasksWithFiltersAndDetails(
                        Task.Status status,
                        Task.TaskType taskType,
                        Task.Priority priority,
                        Long assignedStaffId,
                        Long clientId,
                        String searchTerm,
                        Pageable pageable);

        List<UpcomingDeadlineDTO> getUpcomingDeadlinesByClient(Long clientId);

        TaskStatisticsDTO getTaskStatisticsByClient(Long clientId);
}