package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.NotificationDTO;
import com.adspeek.authservice.entity.Notification;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {

    // Create a new notification
    NotificationDTO createNotification(Long userId, String title, String message,
            Notification.NotificationType type, Long taskId, Long documentId);

    // Get notifications for a user with pagination
    Page<NotificationDTO> getUserNotifications(Long userId, int page, int size);

    // Get unread notifications for a user
    List<NotificationDTO> getUnreadNotifications(Long userId);

    // Count unread notifications for a user
    long getUnreadNotificationCount(Long userId);

    // Mark notifications as read
    void markNotificationsAsRead(List<Long> notificationIds);

    // Mark all notifications as read for a user
    void markAllNotificationsAsRead(Long userId);

    // Delete old read notifications
    void deleteOldNotifications(int daysOld);

    // System notification methods for different events
    void notifyTaskAssigned(Long taskId, Long staffId);

    void notifyTaskStatusChanged(Long taskId, String oldStatus, String newStatus, Long updatedBy);

    void notifyDocumentUploaded(Long documentId, Long clientId);

    void notifyDocumentUploadedByStaff(Long documentId, Long clientId, Long staffId);

    void notifyDocumentVerified(Long documentId, Long verifiedBy);

    void notifyDocumentRejected(Long documentId, Long rejectedBy, String reason);

    void notifyStaffAssignedToClient(Long clientId, Long staffId);

    void notifyTaskCompleted(Long taskId, Long completedBy);

    void notifyDeadlineReminder(Long taskId);

    void notifyMessageReceived(Long fromUserId, Long toUserId, String message);

    // Lead-related notification methods
    void notifyLeadAssigned(Long leadId, Long staffId);

    void notifyNewLeadCreated(Long leadId);

    void notifyNewLeadCreatedForAdmin(Long leadId);

    void notifyLeadConvertedToClient(Long leadId, Long clientId);

    // Service Request notification methods
    void notifyServiceRequestCreated(Long serviceRequestId, Long createdByUserId);

    void notifyServiceRequestStatusChanged(Long serviceRequestId, String oldStatus, String newStatus,
            Long updatedByUserId);

    void notifyServiceRequestAssigned(Long serviceRequestId, Long clientId, Long staffId);

    void notifyServiceRequestRejected(Long serviceRequestId, Long clientId, Long rejectedByUserId);

    void notifyServiceRequestCancelled(Long serviceRequestId, Long clientId, Long cancelledByUserId,
            Long assignedStaffId);

    void notifyServiceRequestConvertedToTask(Long serviceRequestId, Long clientId, Long taskId);
}
