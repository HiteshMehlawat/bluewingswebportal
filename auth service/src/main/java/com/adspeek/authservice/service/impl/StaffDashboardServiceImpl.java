package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.StaffDashboardStatsDTO;
import com.adspeek.authservice.dto.StaffDashboardActivityDTO;
import com.adspeek.authservice.dto.TaskDTO;
import com.adspeek.authservice.entity.Staff;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.entity.StaffActivity;
import com.adspeek.authservice.entity.Task;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.repository.TaskRepository;
import com.adspeek.authservice.repository.DocumentRepository;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.repository.StaffActivityRepository;
import com.adspeek.authservice.service.StaffDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffDashboardServiceImpl implements StaffDashboardService {

    private final StaffRepository staffRepository;
    private final TaskRepository taskRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final StaffActivityRepository staffActivityRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return null;
    }

    private Staff getCurrentStaff() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }

        // Find staff by user ID
        Optional<Staff> staff = staffRepository.findAll().stream()
                .filter(s -> s.getUser() != null && s.getUser().getId().equals(currentUser.getId()))
                .findFirst();

        if (staff.isEmpty()) {
            throw new RuntimeException("Staff not found for current user");
        }

        return staff.get();
    }

    @Override
    public StaffDashboardStatsDTO getDashboardStats() {
        try {
            Staff currentStaff = getCurrentStaff();
            Long staffId = currentStaff.getId();

            // Get task statistics for current staff
            List<Object[]> taskStats = taskRepository.getTaskStatisticsByStaff(staffId, LocalDate.now());
            Object[] stats = taskStats.isEmpty() ? new Object[7] : taskStats.get(0);

            // Get unique clients count for the staff member
            Long uniqueClientsCount = taskRepository.getUniqueClientsCountByStaff(staffId);
            if (uniqueClientsCount == null) {
                uniqueClientsCount = 0L;
            }

            // Calculate average completion time from actual data
            Double avgCompletionTime = taskRepository.getAverageCompletionTimeByStaff(staffId);
            if (avgCompletionTime == null || avgCompletionTime == 0.0) {
                avgCompletionTime = 0.0; // Default to 0 if no completed tasks
            }

            return StaffDashboardStatsDTO.builder()
                    .totalAssignedTasks(getLongValue(stats, 0, 0L)) // totalTasks
                    .pendingTasks(getLongValue(stats, 1, 0L)) // pendingTasks
                    .inProgressTasks(getLongValue(stats, 2, 0L)) // inProgressTasks
                    .completedTasks(getLongValue(stats, 3, 0L)) // completedTasks
                    .onHoldTasks(getLongValue(stats, 4, 0L)) // onHoldTasks
                    .cancelledTasks(getLongValue(stats, 5, 0L)) // cancelledTasks
                    .overdueTasks(getLongValue(stats, 6, 0L)) // overdueTasks
                    .totalAssignedClients(uniqueClientsCount) // Use the new method
                    .averageTaskCompletionTime(avgCompletionTime)
                    .build();

        } catch (Exception e) {
            log.error("Error getting staff dashboard stats: {}", e.getMessage(), e);
            return StaffDashboardStatsDTO.builder()
                    .totalAssignedTasks(0L)
                    .pendingTasks(0L)
                    .inProgressTasks(0L)
                    .completedTasks(0L)
                    .onHoldTasks(0L)
                    .cancelledTasks(0L)
                    .overdueTasks(0L)
                    .totalAssignedClients(0L)
                    .averageTaskCompletionTime(0.0)
                    .build();
        }
    }

    @Override
    public Page<TaskDTO> getRecentTasks(int page, int size) {
        try {
            Staff currentStaff = getCurrentStaff();
            Pageable pageable = PageRequest.of(page, size);

            // Use existing method to get tasks by staff and convert to DTOs
            Page<Task> tasks = taskRepository.findByAssignedStaffId(currentStaff.getId(), pageable);
            return tasks.map(this::convertToTaskDTO);
        } catch (Exception e) {
            log.error("Error getting recent tasks: {}", e.getMessage(), e);
            return Page.empty();
        }
    }

    @Override
    public Page<StaffDashboardActivityDTO> getRecentActivities(int page, int size) {
        try {
            Staff currentStaff = getCurrentStaff();
            Pageable pageable = PageRequest.of(page, size);

            // Get recent activities for the current staff member
            Page<StaffActivity> activities = staffActivityRepository.findByStaffId(currentStaff.getId(), pageable);

            // Convert to DTOs
            Page<StaffDashboardActivityDTO> activityDTOs = activities.map(this::convertToActivityDTO);

            return activityDTOs;
        } catch (Exception e) {
            log.error("Error getting recent activities: {}", e.getMessage(), e);
            return Page.empty();
        }
    }

    private StaffDashboardActivityDTO convertToActivityDTO(StaffActivity activity) {
        String title = "";
        String description = "";
        String icon = "";
        String color = "";

        switch (activity.getActivityType()) {
            case TASK_STARTED:
                title = "Task Started";
                description = activity.getTaskDescription() != null ? activity.getTaskDescription()
                        : "Started working on a task";
                icon = "play_arrow";
                color = "text-blue-600";
                break;
            case TASK_COMPLETED:
                title = "Task Completed";
                description = activity.getTaskDescription() != null ? activity.getTaskDescription()
                        : "Completed a task";
                icon = "check_circle";
                color = "text-green-600";
                break;
            case TASK_DELAYED:
                title = "Task Delayed";
                description = activity.getTaskDescription() != null ? activity.getTaskDescription()
                        : "Task was delayed";
                icon = "schedule";
                color = "text-orange-600";
                break;
            case CLIENT_ASSIGNED:
                title = "Client Assigned";
                description = "New client assigned to you";
                icon = "person_add";
                color = "text-purple-600";
                break;
            case DOCUMENT_UPLOADED:
                title = "Document Uploaded";
                description = "Uploaded a document";
                icon = "upload_file";
                color = "text-indigo-600";
                break;
            case CLIENT_CONTACT:
                title = "Client Contacted";
                description = "Contacted a client";
                icon = "phone";
                color = "text-teal-600";
                break;
            case LOGIN:
                title = "Logged In";
                description = "Started work session";
                icon = "login";
                color = "text-gray-600";
                break;
            case LOGOUT:
                title = "Logged Out";
                description = "Ended work session";
                icon = "logout";
                color = "text-gray-600";
                break;
            default:
                title = "Activity";
                description = activity.getTaskDescription() != null ? activity.getTaskDescription()
                        : "Activity performed";
                icon = "info";
                color = "text-gray-600";
        }

        return StaffDashboardActivityDTO.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType().name())
                .title(title)
                .description(description)
                .taskTitle(activity.getTask() != null ? activity.getTask().getTitle() : null)
                .clientName(
                        activity.getClient() != null
                                ? activity.getClient().getUser().getFirstName() + " "
                                        + activity.getClient().getUser().getLastName()
                                : null)
                .workStatus(activity.getWorkStatus() != null ? activity.getWorkStatus().name() : null)
                .timestamp(activity.getCreatedAt())
                .icon(icon)
                .color(color)
                .build();
    }

    private Long getLongValue(Object[] array, int index, Long defaultValue) {
        try {
            if (array != null && array.length > index && array[index] != null) {
                if (array[index] instanceof Number) {
                    return ((Number) array[index]).longValue();
                }
                return Long.parseLong(array[index].toString());
            }
        } catch (Exception e) {
            log.warn("Error parsing long value at index {}: {}", index, e.getMessage());
        }
        return defaultValue;
    }

    private TaskDTO convertToTaskDTO(Task task) {
        if (task == null)
            return null;
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .clientId(task.getClient() != null ? task.getClient().getId() : null)
                .clientName(task.getClient() != null && task.getClient().getUser() != null
                        ? task.getClient().getUser().getFirstName() + " " + task.getClient().getUser().getLastName()
                        : null)
                .assignedStaffId(task.getAssignedStaff() != null ? task.getAssignedStaff().getId() : null)
                .assignedStaffName(task.getAssignedStaff() != null && task.getAssignedStaff().getUser() != null
                        ? task.getAssignedStaff().getUser().getFirstName() + " "
                                + task.getAssignedStaff().getUser().getLastName()
                        : null)
                .assignedStaffEmployeeId(
                        task.getAssignedStaff() != null ? task.getAssignedStaff().getEmployeeId() : null)
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .assignedDate(task.getAssignedDate())
                .startedDate(task.getStartedDate())
                .completedDate(task.getCompletedDate())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .createdBy(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .createdByName(task.getCreatedBy() != null
                        ? task.getCreatedBy().getFirstName() + " " + task.getCreatedBy().getLastName()
                        : null)
                .createdByEmail(task.getCreatedBy() != null ? task.getCreatedBy().getEmail() : null)
                .updatedBy(task.getUpdatedBy() != null ? task.getUpdatedBy().getId() : null)
                .updatedByName(task.getUpdatedBy() != null
                        ? task.getUpdatedBy().getFirstName() + " " + task.getUpdatedBy().getLastName()
                        : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .serviceItemId(task.getServiceItem() != null ? task.getServiceItem().getId() : null)
                .build();
    }
}
