package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.StaffActivityDTO;
import com.adspeek.authservice.entity.StaffActivity;
import com.adspeek.authservice.entity.Staff;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.entity.Client;
import com.adspeek.authservice.entity.Task;
import com.adspeek.authservice.repository.StaffActivityRepository;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.repository.ClientRepository;
import com.adspeek.authservice.repository.TaskRepository;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.service.StaffActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffActivityServiceImpl implements StaffActivityService {

    private final StaffActivityRepository staffActivityRepository;
    private final StaffRepository staffRepository;
    private final ClientRepository clientRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return null;
    }

    @Override
    public StaffActivityDTO createActivity(StaffActivityDTO activityDTO) {
        Staff staff = staffRepository.findById(activityDTO.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        User currentUser = getCurrentUser();

        StaffActivity activity = StaffActivity.builder()
                .staff(staff)
                .activityType(StaffActivity.ActivityType.valueOf(activityDTO.getActivityType()))
                .taskDescription(activityDTO.getTaskDescription())
                .workStatus(activityDTO.getWorkStatus() != null
                        ? StaffActivity.WorkStatus.valueOf(activityDTO.getWorkStatus())
                        : StaffActivity.WorkStatus.PENDING)
                .logDate(activityDTO.getLogDate() != null ? activityDTO.getLogDate() : LocalDate.now())
                .loginTime(activityDTO.getLoginTime())
                .logoutTime(activityDTO.getLogoutTime())
                .durationMinutes(activityDTO.getDurationMinutes())
                .notes(activityDTO.getNotes())
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        // Set client if provided
        if (activityDTO.getClientId() != null) {
            Client client = clientRepository.findById(activityDTO.getClientId()).orElse(null);
            activity.setClient(client);
        }

        // Set task if provided
        if (activityDTO.getTaskId() != null) {
            Task task = taskRepository.findById(activityDTO.getTaskId()).orElse(null);
            activity.setTask(task);
        }

        StaffActivity savedActivity = staffActivityRepository.save(activity);

        return convertToDTO(savedActivity);
    }

    @Override
    public StaffActivityDTO updateActivity(Long id, StaffActivityDTO activityDTO) {
        StaffActivity activity = staffActivityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        User currentUser = getCurrentUser();

        activity.setActivityType(StaffActivity.ActivityType.valueOf(activityDTO.getActivityType()));
        activity.setTaskDescription(activityDTO.getTaskDescription());
        activity.setWorkStatus(StaffActivity.WorkStatus.valueOf(activityDTO.getWorkStatus()));
        activity.setLogDate(activityDTO.getLogDate());
        activity.setLoginTime(activityDTO.getLoginTime());
        activity.setLogoutTime(activityDTO.getLogoutTime());
        activity.setDurationMinutes(activityDTO.getDurationMinutes());
        activity.setNotes(activityDTO.getNotes());
        activity.setUpdatedBy(currentUser);

        // Update client if provided
        if (activityDTO.getClientId() != null) {
            Client client = clientRepository.findById(activityDTO.getClientId()).orElse(null);
            activity.setClient(client);
        }

        // Update task if provided
        if (activityDTO.getTaskId() != null) {
            Task task = taskRepository.findById(activityDTO.getTaskId()).orElse(null);
            activity.setTask(task);
        }

        StaffActivity updatedActivity = staffActivityRepository.save(activity);
        return convertToDTO(updatedActivity);
    }

    @Override
    public void deleteActivity(Long id) {
        staffActivityRepository.deleteById(id);
    }

    @Override
    public StaffActivityDTO getActivityById(Long id) {
        return staffActivityRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public Page<StaffActivityDTO> getActivitiesByStaff(Long staffId, Pageable pageable) {
        return staffActivityRepository.findByStaffId(staffId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<StaffActivityDTO> getActivitiesByDate(LocalDate date, Pageable pageable) {
        return staffActivityRepository.findByDate(date, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<StaffActivityDTO> getActivitiesByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // This would need a custom query in the repository
        return Page.empty(pageable); // Placeholder
    }

    @Override
    public Page<StaffActivityDTO> getActivitiesByStaffAndDateRange(Long staffId, LocalDate startDate, LocalDate endDate,
            Pageable pageable) {
        return staffActivityRepository.findByStaffIdAndDateRange(staffId, startDate, endDate, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public StaffActivityDTO logLogin(Long staffId) {
        StaffActivityDTO loginActivity = StaffActivityDTO.builder()
                .staffId(staffId)
                .activityType("LOGIN")
                .logDate(LocalDate.now())
                .loginTime(LocalTime.now())
                .taskDescription("Staff logged in")
                .workStatus("COMPLETED")
                .build();

        return createActivity(loginActivity);
    }

    @Override
    public StaffActivityDTO logLogout(Long staffId) {
        // Find the last login activity for today that doesn't have a corresponding
        // logout
        List<StaffActivity> loginActivities = staffActivityRepository
                .findLoginActivitiesByStaffAndDate(staffId, LocalDate.now());

        LocalTime loginTime = null;
        if (!loginActivities.isEmpty()) {
            // Find the most recent login that doesn't have a corresponding logout
            for (int i = loginActivities.size() - 1; i >= 0; i--) {
                StaffActivity loginActivity = loginActivities.get(i);
                // Check if there's a logout activity after this login
                List<StaffActivity> logoutActivities = staffActivityRepository
                        .findLogoutActivitiesByStaffAndDate(staffId, LocalDate.now());

                boolean hasCorrespondingLogout = logoutActivities.stream()
                        .anyMatch(logout -> logout.getLogoutTime() != null &&
                                logout.getLogoutTime().isAfter(loginActivity.getLoginTime()));

                if (!hasCorrespondingLogout) {
                    loginTime = loginActivity.getLoginTime();
                    break;
                }
            }
        }

        LocalTime logoutTime = LocalTime.now();
        int durationMinutes = 0;

        if (loginTime != null) {
            durationMinutes = (int) ChronoUnit.MINUTES.between(loginTime, logoutTime);
        }

        StaffActivityDTO logoutActivity = StaffActivityDTO.builder()
                .staffId(staffId)
                .activityType("LOGOUT")
                .logDate(LocalDate.now())
                .logoutTime(logoutTime)
                .durationMinutes(durationMinutes)
                .taskDescription("Staff logged out")
                .workStatus("COMPLETED")
                .build();

        return createActivity(logoutActivity);
    }

    @Override
    public StaffActivityDTO startTask(Long staffId, String taskDescription, Long taskId, Long clientId) {
        StaffActivityDTO taskActivity = StaffActivityDTO.builder()
                .staffId(staffId)
                .activityType("TASK_STARTED")
                .taskDescription(taskDescription)
                .taskId(taskId)
                .clientId(clientId)
                .logDate(LocalDate.now())
                .workStatus("IN_PROGRESS")
                .build();

        return createActivity(taskActivity);
    }

    @Override
    public StaffActivityDTO completeTask(Long staffId, String taskDescription, Long taskId, Long clientId) {
        StaffActivityDTO taskActivity = StaffActivityDTO.builder()
                .staffId(staffId)
                .activityType("TASK_COMPLETED")
                .taskDescription(taskDescription)
                .taskId(taskId)
                .clientId(clientId)
                .logDate(LocalDate.now())
                .workStatus("COMPLETED")
                .build();

        return createActivity(taskActivity);
    }

    @Override
    public StaffActivityDTO delayTask(Long staffId, String taskDescription, Long taskId, Long clientId, String reason) {
        StaffActivityDTO taskActivity = StaffActivityDTO.builder()
                .staffId(staffId)
                .activityType("TASK_DELAYED")
                .taskDescription(taskDescription + " - Reason: " + reason)
                .taskId(taskId)
                .clientId(clientId)
                .logDate(LocalDate.now())
                .workStatus("DELAYED")
                .notes(reason)
                .build();

        return createActivity(taskActivity);
    }

    @Override
    public Map<String, Object> getDailySummary(Long staffId, LocalDate date) {
        List<Object[]> summary = staffActivityRepository.getDailySummaryByStaff(staffId, date, date);

        Map<String, Object> result = new HashMap<>();
        if (!summary.isEmpty()) {
            Object[] row = summary.get(0);
            int totalActivities = ((Number) row[1]).intValue();
            int loginCount = ((Number) row[2]).intValue();
            int logoutCount = ((Number) row[3]).intValue();
            int completedTasks = ((Number) row[4]).intValue();
            int pendingTasks = ((Number) row[5]).intValue();
            int delayedTasks = ((Number) row[6]).intValue();
            int totalDurationMinutes = ((Number) row[7]).intValue();

            result.put("logDate", row[0]);
            result.put("totalActivities", totalActivities);
            result.put("loginCount", loginCount);
            result.put("logoutCount", logoutCount);
            result.put("completedTasks", completedTasks);
            result.put("pendingTasks", pendingTasks);
            result.put("delayedTasks", delayedTasks);
            result.put("totalDurationMinutes", totalDurationMinutes);
            result.put("totalWorkHours", (double) totalDurationMinutes / 60.0);
            result.put("averageWorkHours", totalDurationMinutes > 0 ? (double) totalDurationMinutes / 60.0 : 0.0);
        } else {
            result.put("logDate", date);
            result.put("totalActivities", 0);
            result.put("loginCount", 0);
            result.put("logoutCount", 0);
            result.put("completedTasks", 0);
            result.put("pendingTasks", 0);
            result.put("delayedTasks", 0);
            result.put("totalDurationMinutes", 0);
            result.put("totalWorkHours", 0.0);
            result.put("averageWorkHours", 0.0);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getWeeklySummary(Long staffId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> summary = staffActivityRepository.getDailySummaryByStaff(staffId, startDate, endDate);

        return summary.stream().map(row -> {
            Map<String, Object> daySummary = new HashMap<>();
            int totalActivities = ((Number) row[1]).intValue();
            int loginCount = ((Number) row[2]).intValue();
            int logoutCount = ((Number) row[3]).intValue();
            int completedTasks = ((Number) row[4]).intValue();
            int pendingTasks = ((Number) row[5]).intValue();
            int delayedTasks = ((Number) row[6]).intValue();
            int totalDurationMinutes = ((Number) row[7]).intValue();

            daySummary.put("logDate", row[0]);
            daySummary.put("totalActivities", totalActivities);
            daySummary.put("loginCount", loginCount);
            daySummary.put("logoutCount", logoutCount);
            daySummary.put("completedTasks", completedTasks);
            daySummary.put("pendingTasks", pendingTasks);
            daySummary.put("delayedTasks", delayedTasks);
            daySummary.put("totalDurationMinutes", totalDurationMinutes);
            daySummary.put("totalWorkHours", (double) totalDurationMinutes / 60.0);
            return daySummary;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getWorkloadSummary(LocalDate date) {
        List<Object[]> summary = staffActivityRepository.getWorkloadSummaryByDate(date);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> staffWorkloads = new ArrayList<>();

        for (Object[] row : summary) {
            Map<String, Object> staffWorkload = new HashMap<>();
            int totalActivities = ((Number) row[4]).intValue();
            int completedTasks = ((Number) row[5]).intValue();
            int pendingTasks = ((Number) row[6]).intValue();
            int delayedTasks = ((Number) row[7]).intValue();
            int assignedClients = row[8] != null ? ((Number) row[8]).intValue() : 0;
            int totalWorkMinutes = ((Number) row[9]).intValue();

            staffWorkload.put("staffId", row[0]);
            staffWorkload.put("staffName", row[1]);
            staffWorkload.put("employeeId", row[2]);
            staffWorkload.put("department", row[3]);
            staffWorkload.put("totalActivities", totalActivities);
            staffWorkload.put("completedTasks", completedTasks);
            staffWorkload.put("pendingTasks", pendingTasks);
            staffWorkload.put("delayedTasks", delayedTasks);
            staffWorkload.put("assignedClients", assignedClients);
            staffWorkload.put("totalWorkMinutes", totalWorkMinutes);
            staffWorkload.put("totalWorkHours", (double) totalWorkMinutes / 60.0);
            staffWorkload.put("lastActivity", row[10]);
            staffWorkloads.add(staffWorkload);
        }

        result.put("date", date);
        result.put("staffWorkloads", staffWorkloads);
        result.put("totalStaff", staffWorkloads.size());

        return result;
    }

    @Override
    public Map<String, Object> getStaffPerformance(Long staffId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> summary = staffActivityRepository.getDailySummaryByStaff(staffId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        int totalActivities = 0;
        int totalCompletedTasks = 0;
        int totalPendingTasks = 0;
        int totalDelayedTasks = 0;
        int totalWorkMinutes = 0;
        int totalLoginCount = 0;
        int totalLogoutCount = 0;

        for (Object[] row : summary) {
            totalActivities += ((Number) row[1]).intValue();
            totalLoginCount += ((Number) row[2]).intValue();
            totalLogoutCount += ((Number) row[3]).intValue();
            totalCompletedTasks += ((Number) row[4]).intValue();
            totalPendingTasks += ((Number) row[5]).intValue();
            totalDelayedTasks += ((Number) row[6]).intValue();
            totalWorkMinutes += ((Number) row[7]).intValue();
        }

        // Calculate average work hours per day
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double averageWorkHoursPerDay = daysBetween > 0 ? (double) totalWorkMinutes / 60.0 / daysBetween : 0.0;

        // Calculate additional metrics
        double totalWorkHours = (double) totalWorkMinutes / 60.0;
        int completedSessions = Math.min(totalLoginCount, totalLogoutCount); // Completed login/logout pairs
        double averageSessionLength = completedSessions > 0 ? totalWorkHours / completedSessions : 0.0;

        result.put("staffId", staffId);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalActivities", totalActivities);
        result.put("totalCompletedTasks", totalCompletedTasks);
        result.put("totalPendingTasks", totalPendingTasks);
        result.put("totalDelayedTasks", totalDelayedTasks);
        result.put("totalWorkMinutes", totalWorkMinutes);
        result.put("totalLoginCount", totalLoginCount);
        result.put("totalLogoutCount", totalLogoutCount);
        result.put("completedSessions", completedSessions);
        result.put("averageWorkHoursPerDay", averageWorkHoursPerDay);
        result.put("totalWorkHours", totalWorkHours);
        result.put("averageSessionLength", averageSessionLength);

        return result;
    }

    private StaffActivityDTO convertToDTO(StaffActivity activity) {
        StaffActivityDTO.StaffActivityDTOBuilder builder = StaffActivityDTO.builder()
                .id(activity.getId())
                .staffId(activity.getStaff().getId())
                .staffName(activity.getStaff().getUser().getFirstName() + " "
                        + activity.getStaff().getUser().getLastName())
                .activityType(activity.getActivityType().name())
                .taskDescription(activity.getTaskDescription())
                .workStatus(activity.getWorkStatus().name())
                .logDate(activity.getLogDate())
                .loginTime(activity.getLoginTime())
                .logoutTime(activity.getLogoutTime())
                .durationMinutes(activity.getDurationMinutes())
                .notes(activity.getNotes())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt());

        if (activity.getClient() != null) {
            builder.clientId(activity.getClient().getId());
            builder.clientName(
                    activity.getClient().getUser().getFirstName() + " " + activity.getClient().getUser().getLastName());
        }

        if (activity.getTask() != null) {
            builder.taskId(activity.getTask().getId());
            builder.taskTitle(activity.getTask().getTitle());
        }

        if (activity.getCreatedBy() != null) {
            builder.createdBy(activity.getCreatedBy().getFirstName() + " " + activity.getCreatedBy().getLastName());
        }

        if (activity.getUpdatedBy() != null) {
            builder.updatedBy(activity.getUpdatedBy().getFirstName() + " " + activity.getUpdatedBy().getLastName());
        }

        return builder.build();
    }
}