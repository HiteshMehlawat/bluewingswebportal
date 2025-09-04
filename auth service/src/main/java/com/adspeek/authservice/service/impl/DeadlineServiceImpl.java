package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.DeadlineDTO;
import com.adspeek.authservice.dto.DeadlineStatisticsDTO;
import com.adspeek.authservice.entity.Task;
import com.adspeek.authservice.repository.TaskRepository;
import com.adspeek.authservice.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeadlineServiceImpl implements DeadlineService {

    private final TaskRepository taskRepository;

    @Override
    public Page<DeadlineDTO> getDeadlines(Long clientId, String status, String priority, String taskType,
            Boolean isOverdue, String searchTerm, Pageable pageable) {
        // This will use the existing task repository to get tasks and convert them to
        // deadlines
        // The deadline is essentially a view of tasks with additional calculated fields
        LocalDateTime today = LocalDate.now().atStartOfDay();

        Page<Object[]> taskResults = taskRepository
                .findTasksWithDeadlineInfo(clientId, status, priority, taskType, isOverdue, searchTerm,
                        today,
                        pageable);

        // Map Object[] results to DeadlineDTO
        List<DeadlineDTO> deadlineDTOs = taskResults.getContent().stream()
                .map(this::mapObjectArrayToDeadlineDTO)
                .collect(Collectors.toList());

        // Filter by overdue status if specified
        if (isOverdue != null) {
            deadlineDTOs = deadlineDTOs.stream()
                    .filter(deadline -> isOverdue.equals(deadline.getIsOverdue()))
                    .collect(Collectors.toList());
        }

        return new org.springframework.data.domain.PageImpl<>(
                deadlineDTOs,
                pageable,
                taskResults.getTotalElements());
    }

    @Override
    public Page<DeadlineDTO> getClientDeadlines(Long clientId, String status, String priority, String taskType,
            Boolean isOverdue, String searchTerm, Pageable pageable) {
        LocalDateTime today = LocalDate.now().atStartOfDay();

        Page<Object[]> taskResults = taskRepository
                .findTasksWithDeadlineInfo(clientId, status, priority, taskType, isOverdue, searchTerm,
                        today,
                        pageable);

        // Map Object[] results to DeadlineDTO
        List<DeadlineDTO> deadlineDTOs = taskResults.getContent().stream()
                .map(this::mapObjectArrayToDeadlineDTO)
                .collect(Collectors.toList());

        // Filter by overdue status if specified
        if (isOverdue != null) {
            deadlineDTOs = deadlineDTOs.stream()
                    .filter(deadline -> isOverdue.equals(deadline.getIsOverdue()))
                    .collect(Collectors.toList());
        }

        return new org.springframework.data.domain.PageImpl<>(
                deadlineDTOs,
                pageable,
                taskResults.getTotalElements());
    }

    @Override
    public List<DeadlineDTO> getOverdueDeadlines() {
        LocalDate today = LocalDate.now();
        return taskRepository.findOverdueTasks(today)
                .stream()
                .map(this::mapTaskToDeadlineDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeadlineDTO> getDueSoonDeadlines() {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        return taskRepository.findTasksDueSoon(today, weekFromNow)
                .stream()
                .map(this::mapTaskToDeadlineDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DeadlineStatisticsDTO getDeadlineStatistics() {
        LocalDateTime today = LocalDate.now().atStartOfDay();

        Long totalDeadlines = taskRepository.countAllTasks();
        Long pendingDeadlines = taskRepository.countByStatus(Task.Status.PENDING.toString());
        Long inProgressDeadlines = taskRepository.countByStatus(Task.Status.IN_PROGRESS.toString());
        Long completedDeadlines = taskRepository.countByStatus(Task.Status.COMPLETED.toString());
        Long overdueDeadlines = taskRepository.countOverdueTasks(today.toLocalDate());
        Long dueSoonDeadlines = taskRepository.countDueSoonTasks(today.toLocalDate(), today.toLocalDate().plusDays(7));
        Long safeDeadlines = totalDeadlines - overdueDeadlines - dueSoonDeadlines;

        return DeadlineStatisticsDTO.builder()
                .totalDeadlines(totalDeadlines)
                .pendingDeadlines(pendingDeadlines)
                .inProgressDeadlines(inProgressDeadlines)
                .completedDeadlines(completedDeadlines)
                .overdueDeadlines(overdueDeadlines)
                .dueSoonDeadlines(dueSoonDeadlines)
                .safeDeadlines(safeDeadlines)
                .build();
    }

    @Override
    public DeadlineDTO getDeadlineById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return mapTaskToDeadlineDTO(task);
    }

    @Override
    public DeadlineDTO updateDeadlineStatus(Long deadlineId, String status) {
        Task task = taskRepository.findById(deadlineId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(Task.Status.valueOf(status));
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return mapTaskToDeadlineDTO(updatedTask);
    }

    @Override
    public DeadlineDTO extendDeadline(Long deadlineId, String newDueDate) {
        Task task = taskRepository.findById(deadlineId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        LocalDate newDueDateLocal = LocalDate.parse(newDueDate);
        task.setDueDate(newDueDateLocal);
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return mapTaskToDeadlineDTO(updatedTask);
    }

    @Override
    public List<DeadlineDTO> getDeadlinesByDateRange(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return taskRepository.findTasksByDateRange(start, end)
                .stream()
                .map(this::mapTaskToDeadlineDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeadlineDTO> getUpcomingDeadlines(int limit) {
        LocalDate today = LocalDate.now();
        return taskRepository.findUpcomingTasks(today)
                .stream()
                .limit(limit)
                .map(this::mapTaskToDeadlineDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void sendDeadlineReminder(Long deadlineId) {
        // This would integrate with email/SMS service
        // For now, just log the reminder
        System.out.println("Sending reminder for deadline ID: " + deadlineId);
    }

    private DeadlineDTO mapTaskToDeadlineDTO(Task task) {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = task.getDueDate();

        int daysRemaining = (int) ChronoUnit.DAYS.between(today, dueDate);
        boolean isOverdue = daysRemaining < 0;

        String deadlineStatus;
        if (isOverdue) {
            deadlineStatus = "OVERDUE";
        } else if (daysRemaining <= 7) {
            deadlineStatus = "DUE_SOON";
        } else {
            deadlineStatus = "SAFE";
        }

        return DeadlineDTO.builder()
                .id(task.getId())
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .taskDescription(task.getDescription())
                .dueDate(task.getDueDate().atStartOfDay())
                .status(task.getStatus().toString())
                .priority(task.getPriority().toString())
                .taskType(task.getTaskType().toString())
                .clientId(task.getClient().getId())
                .clientName(task.getClient().getCompanyName())
                .assignedStaffId(task.getAssignedStaff() != null ? task.getAssignedStaff().getId() : null)
                .assignedStaffName(
                        task.getAssignedStaff() != null
                                ? task.getAssignedStaff().getUser().getFirstName() + " "
                                        + task.getAssignedStaff().getUser().getLastName()
                                : null)
                .daysRemaining(daysRemaining)
                .isOverdue(isOverdue)
                .deadlineStatus(deadlineStatus)
                .hasDocuments(false) // Document task relationship not implemented yet
                .latestComment(null) // TaskComment entity not implemented yet
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private DeadlineDTO mapObjectArrayToDeadlineDTO(Object[] row) {
        if (row == null || row.length < 23) { // Updated to 23 based on the actual column count
            return null;
        }

        try {
            LocalDate today = LocalDate.now();
            LocalDate dueDate = row[8] != null ? ((java.sql.Date) row[8]).toLocalDate() : null;

            int daysRemaining = 0;
            boolean isOverdue = false;
            if (dueDate != null) {
                daysRemaining = (int) ChronoUnit.DAYS.between(today, dueDate);
                isOverdue = daysRemaining < 0;
            }

            String deadlineStatus;
            if (isOverdue) {
                deadlineStatus = "OVERDUE";
            } else if (daysRemaining <= 7) {
                deadlineStatus = "DUE_SOON";
            } else {
                deadlineStatus = "SAFE";
            }

            return DeadlineDTO.builder()
                    .id(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .taskId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .taskTitle((String) row[1])
                    .taskDescription((String) row[2])
                    .dueDate(dueDate != null ? dueDate.atStartOfDay() : null)
                    .status((String) row[6])
                    .priority((String) row[7])
                    .taskType((String) row[5])
                    .clientId(row[3] != null ? ((Number) row[3]).longValue() : null)
                    .clientName((String) row[18]) // Fixed: was 17, should be 18
                    .assignedStaffId(row[4] != null ? ((Number) row[4]).longValue() : null)
                    .assignedStaffName((String) row[19]) // Fixed: was 18, should be 19
                    .daysRemaining(daysRemaining)
                    .isOverdue(isOverdue)
                    .deadlineStatus(deadlineStatus)
                    .hasDocuments(false) // Document task relationship not implemented yet
                    .latestComment(null) // TaskComment entity not implemented yet
                    .createdAt(row[16] != null ? ((java.sql.Timestamp) row[16]).toLocalDateTime() : null) // Fixed: was
                                                                                                          // 15, should
                                                                                                          // be 16
                    .updatedAt(row[17] != null ? ((java.sql.Timestamp) row[17]).toLocalDateTime() : null) // Fixed: was
                                                                                                          // 16, should
                                                                                                          // be 17
                    .build();
        } catch (Exception e) {
            System.err.println("Error mapping task row to DeadlineDTO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
