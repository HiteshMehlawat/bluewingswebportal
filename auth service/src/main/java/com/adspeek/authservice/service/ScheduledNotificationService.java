package com.adspeek.authservice.service;

public interface ScheduledNotificationService {

    // Send deadline reminders for tasks due within 24 hours
    void sendDeadlineReminders();

    // Send overdue task notifications
    void sendOverdueTaskNotifications();

    // Send weekly task summary notifications
    void sendWeeklyTaskSummaries();
}
