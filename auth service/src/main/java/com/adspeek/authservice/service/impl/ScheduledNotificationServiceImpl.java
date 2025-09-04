package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.entity.Task;
import com.adspeek.authservice.repository.TaskRepository;
import com.adspeek.authservice.service.NotificationService;
import com.adspeek.authservice.service.ScheduledNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledNotificationServiceImpl implements ScheduledNotificationService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    // Run every hour to check for deadline reminders
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Override
    public void sendDeadlineReminders() {
        try {
            log.info("Starting deadline reminder check...");

            // Get tasks due within the next 24 hours
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            LocalDateTime now = LocalDateTime.now();

            List<Task> tasksDueSoon = taskRepository.findTasksDueWithin24Hours(now, tomorrow);

            for (Task task : tasksDueSoon) {
                if (task.getStatus() != Task.Status.COMPLETED && task.getStatus() != Task.Status.CANCELLED) {
                    notificationService.notifyDeadlineReminder(task.getId());
                    log.info("Sent deadline reminder for task: {}", task.getTitle());
                }
            }

            log.info("Completed deadline reminder check. Sent {} reminders.", tasksDueSoon.size());
        } catch (Exception e) {
            log.error("Error sending deadline reminders: {}", e.getMessage(), e);
        }
    }

    // Run daily at 9 AM to check for overdue tasks
    @Scheduled(cron = "0 0 9 * * ?") // Every day at 9:00 AM
    @Override
    public void sendOverdueTaskNotifications() {
        try {
            log.info("Starting overdue task notification check...");

            LocalDateTime now = LocalDateTime.now();
            List<Task> overdueTasks = taskRepository.findOverdueTasks(now);

            for (Task task : overdueTasks) {
                if (task.getStatus() != Task.Status.COMPLETED && task.getStatus() != Task.Status.CANCELLED) {
                    // Create custom overdue notification
                    String title = "Task Overdue";
                    String message = String.format(
                            "Your task '%s' is overdue. Please update the status or contact the client.",
                            task.getTitle());

                    // Notify assigned staff
                    if (task.getAssignedStaff() != null) {
                        notificationService.createNotification(
                                task.getAssignedStaff().getUser().getId(),
                                title,
                                message,
                                com.adspeek.authservice.entity.Notification.NotificationType.DEADLINE_REMINDER,
                                task.getId(),
                                null);
                    }

                    // Notify client
                    notificationService.createNotification(
                            task.getClient().getUser().getId(),
                            "Task Overdue",
                            String.format("Your task '%s' is overdue. Please contact your assigned staff for updates.",
                                    task.getTitle()),
                            com.adspeek.authservice.entity.Notification.NotificationType.DEADLINE_REMINDER,
                            task.getId(),
                            null);

                    log.info("Sent overdue notification for task: {}", task.getTitle());
                }
            }

            log.info("Completed overdue task check. Sent {} notifications.", overdueTasks.size());
        } catch (Exception e) {
            log.error("Error sending overdue task notifications: {}", e.getMessage(), e);
        }
    }

    // Run weekly on Monday at 9 AM
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9:00 AM
    @Override
    public void sendWeeklyTaskSummaries() {
        try {
            log.info("Starting weekly task summary generation...");

            LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
            LocalDateTime now = LocalDateTime.now();

            // This would typically involve more complex logic to generate summaries
            // For now, we'll just log that this feature is available
            log.info("Weekly task summary feature is available for implementation");

        } catch (Exception e) {
            log.error("Error generating weekly task summaries: {}", e.getMessage(), e);
        }
    }
}
