package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications by user with pagination
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Find unread notifications by user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Count unread notifications by user
    long countByUserIdAndIsReadFalse(Long userId);

    // Mark notifications as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id IN :notificationIds")
    void markNotificationsAsRead(@Param("notificationIds") List<Long> notificationIds,
            @Param("readAt") LocalDateTime readAt);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.id = :userId AND n.isRead = false")
    void markAllNotificationsAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    // Find notifications by type for a user
    List<Notification> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(Long userId,
            Notification.NotificationType type);

    // Find recent notifications (last 24 hours)
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Delete old notifications (older than specified days)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate AND n.isRead = true")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Find notifications related to a specific task
    List<Notification> findByRelatedTaskIdOrderByCreatedAtDesc(Long taskId);

    // Find notifications related to a specific document
    List<Notification> findByRelatedDocumentIdOrderByCreatedAtDesc(Long documentId);
}
