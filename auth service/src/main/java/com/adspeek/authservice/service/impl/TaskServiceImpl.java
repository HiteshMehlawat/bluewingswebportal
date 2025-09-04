package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.TaskDTO;
import com.adspeek.authservice.dto.TaskDetailDTO;
import com.adspeek.authservice.dto.TaskStatisticsDTO;
import com.adspeek.authservice.dto.StaffWorkloadDTO;
import com.adspeek.authservice.dto.UpcomingDeadlineDTO;
import com.adspeek.authservice.entity.Client;
import com.adspeek.authservice.entity.Staff;
import com.adspeek.authservice.entity.Task;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.entity.ServiceItem;
import com.adspeek.authservice.repository.ClientRepository;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.repository.TaskRepository;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.repository.ServiceItemRepository;
import com.adspeek.authservice.service.TaskService;
import com.adspeek.authservice.service.AuditLogService;
import com.adspeek.authservice.service.NotificationService;
import com.adspeek.authservice.service.StaffActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final StaffActivityService staffActivityService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return null;
    }

    private TaskDTO toDTO(Task task) {
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

    private TaskDetailDTO toDetailDTO(Task task) {
        if (task == null)
            return null;
        return TaskDetailDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .clientId(task.getClient() != null ? task.getClient().getId() : null)
                .clientName(task.getClient() != null && task.getClient().getUser() != null
                        ? task.getClient().getUser().getFirstName() + " " + task.getClient().getUser().getLastName()
                        : null)
                .clientPhone(task.getClient() != null && task.getClient().getUser() != null
                        ? task.getClient().getUser().getPhone()
                        : null)
                .clientEmail(task.getClient() != null && task.getClient().getUser() != null
                        ? task.getClient().getUser().getEmail()
                        : null)
                .assignedStaffId(task.getAssignedStaff() != null ? task.getAssignedStaff().getId() : null)
                .assignedStaffName(task.getAssignedStaff() != null && task.getAssignedStaff().getUser() != null
                        ? task.getAssignedStaff().getUser().getFirstName() + " "
                                + task.getAssignedStaff().getUser().getLastName()
                        : null)
                .assignedStaffEmployeeId(
                        task.getAssignedStaff() != null ? task.getAssignedStaff().getEmployeeId() : null)
                .assignedStaffPhone(task.getAssignedStaff() != null && task.getAssignedStaff().getUser() != null
                        ? task.getAssignedStaff().getUser().getPhone()
                        : null)
                .assignedStaffEmail(task.getAssignedStaff() != null && task.getAssignedStaff().getUser() != null
                        ? task.getAssignedStaff().getUser().getEmail()
                        : null)
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
                // Service information
                .serviceItemId(task.getServiceItem() != null ? task.getServiceItem().getId() : null)
                .serviceItemName(task.getServiceItem() != null ? task.getServiceItem().getName() : null)
                .serviceCategoryName(task.getServiceItem() != null && task.getServiceItem().getSubcategory() != null
                        && task.getServiceItem().getSubcategory().getCategory() != null
                                ? task.getServiceItem().getSubcategory().getCategory().getName()
                                : null)
                .serviceSubcategoryName(task.getServiceItem() != null && task.getServiceItem().getSubcategory() != null
                        ? task.getServiceItem().getSubcategory().getName()
                        : null)
                .build();
    }

    private TaskDTO mapToTaskDTO(Object[] row) {
        if (row == null || row.length < 23) {
            return null;
        }

        try {
            return TaskDTO.builder()
                    .id(((Number) row[0]).longValue())
                    .title((String) row[1])
                    .description((String) row[2])
                    .clientId(((Number) row[3]).longValue())
                    .clientName((String) row[18])
                    .assignedStaffId(row[4] != null ? ((Number) row[4]).longValue() : null)
                    .assignedStaffName((String) row[19])
                    .assignedStaffEmployeeId((String) row[20])
                    .taskType(Task.TaskType.valueOf((String) row[5]))
                    .status(Task.Status.valueOf((String) row[6]))
                    .priority(Task.Priority.valueOf((String) row[7]))
                    .dueDate(((java.sql.Date) row[8]).toLocalDate())
                    .assignedDate(((java.sql.Timestamp) row[9]).toLocalDateTime())
                    .startedDate(row[10] != null ? ((java.sql.Timestamp) row[10]).toLocalDateTime() : null)
                    .completedDate(row[11] != null ? ((java.sql.Timestamp) row[11]).toLocalDateTime() : null)
                    .estimatedHours(row[12] != null ? ((Number) row[12]).doubleValue() : null)
                    .actualHours(row[13] != null ? ((Number) row[13]).doubleValue() : null)
                    .createdBy(((Number) row[14]).longValue())
                    .createdByName((String) row[21])
                    .createdByEmail((String) row[22])
                    .updatedBy(row[15] != null ? ((Number) row[15]).longValue() : null)
                    .updatedByName((String) row[23])
                    .createdAt(((java.sql.Timestamp) row[16]).toLocalDateTime())
                    .updatedAt(((java.sql.Timestamp) row[17]).toLocalDateTime())
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private Task toEntity(TaskDTO dto) {
        if (dto == null)
            return null;
        Client client = dto.getClientId() != null ? clientRepository.findById(dto.getClientId()).orElse(null) : null;
        Staff staff = dto.getAssignedStaffId() != null ? staffRepository.findById(dto.getAssignedStaffId()).orElse(null)
                : null;
        User createdBy = dto.getCreatedBy() != null ? userRepository.findById(dto.getCreatedBy()).orElse(null) : null;
        User updatedBy = dto.getUpdatedBy() != null ? userRepository.findById(dto.getUpdatedBy()).orElse(null) : null;
        return Task.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .client(client)
                .assignedStaff(staff)
                .taskType(dto.getTaskType())
                .status(dto.getStatus())
                .priority(dto.getPriority())
                .dueDate(dto.getDueDate())
                .assignedDate(dto.getAssignedDate())
                .startedDate(dto.getStartedDate())
                .completedDate(dto.getCompletedDate())
                .estimatedHours(dto.getEstimatedHours())
                .actualHours(dto.getActualHours())
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    @Override
    public TaskDTO createTask(TaskDTO taskDTO) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }

        Client client = taskDTO.getClientId() != null ? clientRepository.findById(taskDTO.getClientId()).orElse(null)
                : null;
        Staff staff = taskDTO.getAssignedStaffId() != null
                ? staffRepository.findById(taskDTO.getAssignedStaffId()).orElse(null)
                : null;
        ServiceItem serviceItem = taskDTO.getServiceItemId() != null
                ? serviceItemRepository.findById(taskDTO.getServiceItemId()).orElse(null)
                : null;

        Task task = Task.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .client(client)
                .assignedStaff(staff)
                .serviceItem(serviceItem)
                .taskType(taskDTO.getTaskType())
                .status(taskDTO.getStatus())
                .priority(taskDTO.getPriority())
                .dueDate(taskDTO.getDueDate())
                .assignedDate(taskDTO.getAssignedDate())
                .startedDate(taskDTO.getStartedDate())
                .completedDate(taskDTO.getCompletedDate())
                .estimatedHours(taskDTO.getEstimatedHours())
                .actualHours(taskDTO.getActualHours())
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);

        // Log the activity
        auditLogService.logActivityForCurrentUser(
                "TASK_CREATED",
                "TASK",
                savedTask.getId(),
                null,
                "{\"title\":\"" + savedTask.getTitle() + "\",\"taskType\":\"" + savedTask.getTaskType()
                        + "\",\"priority\":\"" + savedTask.getPriority() + "\"}",
                null,
                null);

        // Send notification to assigned staff
        if (savedTask.getAssignedStaff() != null) {
            notificationService.notifyTaskAssigned(savedTask.getId(), savedTask.getAssignedStaff().getId());
        }

        return toDTO(savedTask);
    }

    @Override
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }

        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty())
            return null;
        Task task = taskOpt.get();

        // Store old values for notification
        String oldStatus = task.getStatus().name();
        Long oldAssignedStaffId = task.getAssignedStaff() != null ? task.getAssignedStaff().getId() : null;

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        if (taskDTO.getAssignedStaffId() != null) {
            task.setAssignedStaff(staffRepository.findById(taskDTO.getAssignedStaffId()).orElse(null));
        }
        if (taskDTO.getServiceItemId() != null) {
            task.setServiceItem(serviceItemRepository.findById(taskDTO.getServiceItemId()).orElse(null));
        }
        task.setTaskType(taskDTO.getTaskType());
        task.setStatus(taskDTO.getStatus());
        task.setPriority(taskDTO.getPriority());
        task.setDueDate(taskDTO.getDueDate());
        task.setStartedDate(taskDTO.getStartedDate());
        task.setCompletedDate(taskDTO.getCompletedDate());
        task.setEstimatedHours(taskDTO.getEstimatedHours());
        task.setActualHours(taskDTO.getActualHours());
        task.setUpdatedBy(currentUser);
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);

        // Send notifications for status changes
        if (!oldStatus.equals(savedTask.getStatus().name())) {
            notificationService.notifyTaskStatusChanged(savedTask.getId(), oldStatus, savedTask.getStatus().name(),
                    currentUser.getId());

            // Send specific notification to client about status change
            notifyClientAboutStatusChange(savedTask, oldStatus, savedTask.getStatus().name());

            // Log staff activity for status changes
            if (savedTask.getAssignedStaff() != null) {
                logStaffActivityForStatusChange(savedTask, oldStatus, savedTask.getStatus().name());
            }
        }

        // Send notification for new staff assignment
        if (oldAssignedStaffId == null && savedTask.getAssignedStaff() != null) {
            notificationService.notifyTaskAssigned(savedTask.getId(), savedTask.getAssignedStaff().getId());
        }

        // Send notification for task completion
        if (savedTask.getStatus() == Task.Status.COMPLETED) {
            notificationService.notifyTaskCompleted(savedTask.getId(), currentUser.getId());
        }

        return toDTO(savedTask);
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Optional<TaskDTO> getTaskById(Long id) {
        return taskRepository.findById(id).map(this::toDTO);
    }

    @Override
    public Optional<TaskDetailDTO> getTaskDetailById(Long id) {
        return taskRepository.findById(id).map(this::toDetailDTO);
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Page<TaskDTO> getTasksWithFilters(
            Task.Status status,
            Task.Priority priority,
            Long assignedStaffId,
            Long clientId,
            String searchTerm,
            Long serviceCategoryId,
            Long serviceSubcategoryId,
            Long serviceItemId,
            Pageable pageable) {

        // Convert enum values to strings for the repository call
        String statusStr = status != null ? status.name() : null;
        String priorityStr = priority != null ? priority.name() : null;

        Page<Object[]> result = taskRepository.findByFiltersWithServiceHierarchy(
                statusStr, priorityStr, assignedStaffId, clientId, searchTerm,
                serviceCategoryId, serviceSubcategoryId, serviceItemId, pageable);

        return result.map(this::mapToTaskDTO);
    }

    @Override
    public Page<TaskDTO> searchTasks(String searchTerm, Pageable pageable) {
        Page<Object[]> searchResults = taskRepository.findBySearchTermNative(searchTerm, pageable);
        return searchResults.map(this::mapToTaskDTO);
    }

    @Override
    public List<TaskDTO> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDate.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> getTasksDueSoon() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        return taskRepository.findTasksDueSoon(today, threeDaysLater).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TaskDTO> getTasksByStaff(Long staffId, Pageable pageable) {
        return taskRepository.findByAssignedStaffId(staffId, pageable).map(this::toDTO);
    }

    @Override
    public Page<TaskDTO> getTasksByStaffAndClient(Long staffId, Long clientId, Pageable pageable) {
        return taskRepository.findByAssignedStaffIdAndClientId(staffId, clientId, pageable).map(this::toDTO);
    }

    @Override
    public TaskStatisticsDTO getTaskStatistics() {
        List<Object[]> statsList = taskRepository.getTaskStatistics(LocalDate.now());

        if (statsList != null && !statsList.isEmpty()) {
            Object[] stats = statsList.get(0); // Get the first (and only) row

            if (stats != null && stats.length >= 7) {
                // Convert each value safely
                Long totalTasks = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
                Long pendingTasks = stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
                Long inProgressTasks = stats[2] != null ? ((Number) stats[2]).longValue() : 0L;
                Long completedTasks = stats[3] != null ? ((Number) stats[3]).longValue() : 0L;
                Long onHoldTasks = stats[4] != null ? ((Number) stats[4]).longValue() : 0L;
                Long cancelledTasks = stats[5] != null ? ((Number) stats[5]).longValue() : 0L;
                Long overdueTasks = stats[6] != null ? ((Number) stats[6]).longValue() : 0L;

                return TaskStatisticsDTO.builder()
                        .totalTasks(totalTasks)
                        .pendingTasks(pendingTasks)
                        .inProgressTasks(inProgressTasks)
                        .completedTasks(completedTasks)
                        .onHoldTasks(onHoldTasks)
                        .cancelledTasks(cancelledTasks)
                        .overdueTasks(overdueTasks)
                        .build();
            }
        }

        // Return default values if query fails or returns null
        return TaskStatisticsDTO.builder()
                .totalTasks(0L)
                .pendingTasks(0L)
                .inProgressTasks(0L)
                .completedTasks(0L)
                .onHoldTasks(0L)
                .cancelledTasks(0L)
                .overdueTasks(0L)
                .build();
    }

    @Override
    public List<StaffWorkloadDTO> getStaffWorkloadSummary() {
        List<Object[]> workloadData = taskRepository.getStaffWorkloadSummary(LocalDate.now());
        return workloadData.stream().map(row -> {
            if (row != null && row.length >= 9) {
                return StaffWorkloadDTO.builder()
                        .staffId(((Number) row[0]).longValue())
                        .staffName((String) row[1])
                        .position((String) row[2])
                        .department((String) row[3])
                        .totalTasks(((Number) row[4]).longValue())
                        .pendingTasks(((Number) row[5]).longValue())
                        .inProgressTasks(((Number) row[6]).longValue())
                        .completedTasks(((Number) row[7]).longValue())
                        .overdueTasks(((Number) row[8]).longValue())
                        .build();
            }
            return null;
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    @Override
    public List<StaffWorkloadDTO> getAvailableStaffForAssignment() {
        List<Object[]> availableStaff = taskRepository.getAvailableStaffForAssignment();
        return availableStaff.stream().map(row -> {
            if (row != null && row.length >= 6) {
                return StaffWorkloadDTO.builder()
                        .staffId(((Number) row[0]).longValue())
                        .staffName((String) row[1])
                        .employeeId((String) row[2])
                        .position((String) row[3])
                        .department((String) row[4])
                        .currentTasks(((Number) row[5]).longValue())
                        .build();
            }
            return null;
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    @Override
    public TaskDTO reassignTask(Long taskId, Long newStaffId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }

        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return null;
        }
        Task task = taskOpt.get();
        Staff newStaff = staffRepository.findById(newStaffId).orElse(null);
        task.setAssignedStaff(newStaff);
        task.setUpdatedBy(currentUser);
        task.setUpdatedAt(LocalDateTime.now());
        return toDTO(taskRepository.save(task));
    }

    @Override
    public TaskDTO updateTaskStatus(Long taskId, Task.Status newStatus) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }

        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return null;
        }
        Task task = taskOpt.get();
        task.setStatus(newStatus);

        // Update timestamps based on status
        if (newStatus == Task.Status.IN_PROGRESS && task.getStartedDate() == null) {
            task.setStartedDate(LocalDateTime.now());
        } else if (newStatus == Task.Status.COMPLETED && task.getCompletedDate() == null) {
            task.setCompletedDate(LocalDateTime.now());
        }

        task.setUpdatedBy(currentUser);
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);

        // Log the activity
        auditLogService.logActivityForCurrentUser(
                "TASK_STATUS_UPDATED",
                "TASK",
                savedTask.getId(),
                null,
                "{\"title\":\"" + savedTask.getTitle() + "\",\"status\":\"" + savedTask.getStatus() + "\"}",
                null,
                null);

        return toDTO(savedTask);
    }

    @Override
    public Page<UpcomingDeadlineDTO> getUpcomingDeadlinesByClient(Long clientId, Pageable pageable) {
        Page<Object[]> page = taskRepository.findUpcomingDeadlinesByClient(clientId, pageable);
        return page.map(row -> {
            return UpcomingDeadlineDTO.builder()
                    .taskId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .taskName((String) row[1])
                    .description((String) row[2])
                    .dueDate(row[3] != null ? ((java.sql.Date) row[3]).toLocalDate() : null)
                    .status(row[4] != null ? row[4].toString() : null)
                    .priority(row[5] != null ? row[5].toString() : null)
                    .taskType(row[6] != null ? row[6].toString() : null)
                    .staffId(row[7] != null ? ((Number) row[7]).longValue() : null)
                    .staffName((String) row[8])
                    .latestRemark((String) row[9])
                    .deadlineStatus((String) row[10])
                    .hasDocuments(row[11] != null && (row[11].toString().equals("1") || row[11].equals(Boolean.TRUE)))
                    .build();
        });
    }

    @Override
    public Page<TaskDTO> getTasksByClient(Long clientId, Pageable pageable) {
        return taskRepository.findByClientId(clientId, pageable).map(this::toDTO);
    }

    @Override
    public List<UpcomingDeadlineDTO> getUpcomingDeadlinesByClient(Long clientId) {
        List<Object[]> deadlines = taskRepository.findUpcomingDeadlinesByClientList(clientId);
        return deadlines.stream().map(row -> {
            return UpcomingDeadlineDTO.builder()
                    .taskId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .taskName((String) row[1])
                    .description((String) row[2])
                    .dueDate(row[3] != null ? ((java.sql.Date) row[3]).toLocalDate() : null)
                    .status(row[4] != null ? row[4].toString() : null)
                    .priority(row[5] != null ? row[5].toString() : null)
                    .taskType(row[6] != null ? row[6].toString() : null)
                    .staffId(row[7] != null ? ((Number) row[7]).longValue() : null)
                    .staffName((String) row[8])
                    .latestRemark((String) row[9])
                    .deadlineStatus((String) row[10])
                    .hasDocuments(row[11] != null && (row[11].toString().equals("1") || row[11].equals(Boolean.TRUE)))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public TaskStatisticsDTO getTaskStatisticsByClient(Long clientId) {
        List<Object[]> statsList = taskRepository.getTaskStatisticsByClient(clientId, LocalDate.now());

        if (statsList != null && !statsList.isEmpty()) {
            Object[] stats = statsList.get(0);

            if (stats != null && stats.length >= 7) {
                Long totalTasks = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
                Long pendingTasks = stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
                Long inProgressTasks = stats[2] != null ? ((Number) stats[2]).longValue() : 0L;
                Long completedTasks = stats[3] != null ? ((Number) stats[3]).longValue() : 0L;
                Long onHoldTasks = stats[4] != null ? ((Number) stats[4]).longValue() : 0L;
                Long cancelledTasks = stats[5] != null ? ((Number) stats[5]).longValue() : 0L;
                Long overdueTasks = stats[6] != null ? ((Number) stats[6]).longValue() : 0L;

                return TaskStatisticsDTO.builder()
                        .totalTasks(totalTasks)
                        .pendingTasks(pendingTasks)
                        .inProgressTasks(inProgressTasks)
                        .completedTasks(completedTasks)
                        .onHoldTasks(onHoldTasks)
                        .cancelledTasks(cancelledTasks)
                        .overdueTasks(overdueTasks)
                        .build();
            }
        }

        return TaskStatisticsDTO.builder()
                .totalTasks(0L)
                .pendingTasks(0L)
                .inProgressTasks(0L)
                .completedTasks(0L)
                .onHoldTasks(0L)
                .cancelledTasks(0L)
                .overdueTasks(0L)
                .build();
    }

    @Override
    public Page<TaskDetailDTO> getTasksByClientWithDetails(Long clientId, Pageable pageable) {
        Page<Object[]> result = taskRepository.findTasksByClientWithDetails(clientId, pageable);
        return result.map(this::mapToTaskDetailDTO);
    }

    @Override
    public Page<TaskDetailDTO> getTasksWithFiltersAndDetails(
            Task.Status status,
            Task.TaskType taskType,
            Task.Priority priority,
            Long assignedStaffId,
            Long clientId,
            String searchTerm,
            Pageable pageable) {

        // Convert enum values to strings for the repository call
        String statusStr = status != null ? status.name() : null;
        String taskTypeStr = taskType != null ? taskType.name() : null;
        String priorityStr = priority != null ? priority.name() : null;

        Page<Object[]> result = taskRepository.findTasksWithDetailsByFilters(
                statusStr, taskTypeStr, priorityStr, assignedStaffId, clientId, searchTerm, pageable);

        return result.map(this::mapToTaskDetailDTO);
    }

    private TaskDetailDTO mapToTaskDetailDTO(Object[] row) {
        if (row == null || row.length < 25) {
            return null;
        }

        try {
            return TaskDetailDTO.builder()
                    .id(((Number) row[0]).longValue())
                    .title((String) row[1])
                    .description((String) row[2])
                    .clientId(((Number) row[3]).longValue())
                    .clientName((String) row[18])
                    .clientPhone((String) row[19])
                    .clientEmail((String) row[20])
                    .assignedStaffId(row[4] != null ? ((Number) row[4]).longValue() : null)
                    .assignedStaffName((String) row[21])
                    .assignedStaffEmployeeId((String) row[22])
                    .assignedStaffPhone((String) row[23])
                    .assignedStaffEmail((String) row[24])
                    .taskType(Task.TaskType.valueOf((String) row[5]))
                    .status(Task.Status.valueOf((String) row[6]))
                    .priority(Task.Priority.valueOf((String) row[7]))
                    .dueDate(((java.sql.Date) row[8]).toLocalDate())
                    .assignedDate(((java.sql.Timestamp) row[9]).toLocalDateTime())
                    .startedDate(row[10] != null ? ((java.sql.Timestamp) row[10]).toLocalDateTime() : null)
                    .completedDate(row[11] != null ? ((java.sql.Timestamp) row[11]).toLocalDateTime() : null)
                    .estimatedHours(row[12] != null ? ((Number) row[12]).doubleValue() : null)
                    .actualHours(row[13] != null ? ((Number) row[13]).doubleValue() : null)
                    .createdBy(((Number) row[14]).longValue())
                    .createdByName((String) row[25])
                    .createdByEmail((String) row[26])
                    .updatedBy(row[15] != null ? ((Number) row[15]).longValue() : null)
                    .updatedByName((String) row[27])
                    .createdAt(((java.sql.Timestamp) row[16]).toLocalDateTime())
                    .updatedAt(((java.sql.Timestamp) row[17]).toLocalDateTime())
                    .deadlineStatus(null) // Will be calculated if needed
                    .hasDocuments(false) // Will be calculated if needed
                    .latestComment(null) // Will be populated if needed
                    // Service information (will be populated from service item if available)
                    .serviceItemId(null)
                    .serviceItemName(null)
                    .serviceCategoryName(null)
                    .serviceSubcategoryName(null)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private void logStaffActivityForStatusChange(Task task, String oldStatus, String newStatus) {
        try {
            String activityDescription = String.format("Task status changed from %s to %s", oldStatus, newStatus);

            switch (Task.Status.valueOf(newStatus)) {
                case IN_PROGRESS:
                    staffActivityService.startTask(
                            task.getAssignedStaff().getId(),
                            activityDescription,
                            task.getId(),
                            task.getClient().getId());
                    break;
                case COMPLETED:
                    staffActivityService.completeTask(
                            task.getAssignedStaff().getId(),
                            activityDescription,
                            task.getId(),
                            task.getClient().getId());
                    break;
                case ON_HOLD:
                    staffActivityService.delayTask(
                            task.getAssignedStaff().getId(),
                            activityDescription,
                            task.getId(),
                            task.getClient().getId(),
                            "Task put on hold");
                    break;
                default:
                    // For other status changes, create a generic activity
                    staffActivityService.createActivity(
                            com.adspeek.authservice.dto.StaffActivityDTO.builder()
                                    .staffId(task.getAssignedStaff().getId())
                                    .activityType("TASK_UPDATED")
                                    .taskDescription(activityDescription)
                                    .workStatus(newStatus)
                                    .logDate(java.time.LocalDate.now())
                                    .taskId(task.getId())
                                    .clientId(task.getClient().getId())
                                    .build());
                    break;
            }
        } catch (Exception e) {
            log.error("Error logging staff activity for task status change: {}", e.getMessage(), e);
        }
    }

    private void notifyClientAboutStatusChange(Task task, String oldStatus, String newStatus) {
        try {
            String title = "Task Status Update";
            String message = String.format(
                    "Your task '%s' has been updated from %s to %s by your assigned staff member.",
                    task.getTitle(), oldStatus, newStatus);

            // Get the client's user ID
            Long clientUserId = task.getClient().getUser().getId();

            // Create notification for the client
            notificationService.createNotification(
                    clientUserId,
                    title,
                    message,
                    com.adspeek.authservice.entity.Notification.NotificationType.STATUS_UPDATE,
                    task.getId(),
                    null);

            log.info("Sent status change notification to client {} for task {}", clientUserId, task.getId());
        } catch (Exception e) {
            log.error("Error sending status change notification to client: {}", e.getMessage(), e);
        }
    }
}