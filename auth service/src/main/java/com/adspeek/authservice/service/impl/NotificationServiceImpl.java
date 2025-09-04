package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.NotificationDTO;
import com.adspeek.authservice.entity.*;
import com.adspeek.authservice.repository.*;
import com.adspeek.authservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DocumentRepository documentRepository;
    private final ClientRepository clientRepository;
    private final StaffRepository staffRepository;

    @Override
    public NotificationDTO createNotification(Long userId, String title, String message,
            Notification.NotificationType type, Long taskId, Long documentId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Task task = null;
            if (taskId != null) {
                task = taskRepository.findById(taskId).orElse(null);
            }

            Document document = null;
            if (documentId != null) {
                document = documentRepository.findById(documentId).orElse(null);
            }

            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .notificationType(type)
                    .isRead(false)
                    .relatedTask(task)
                    .relatedDocument(document)
                    .createdAt(LocalDateTime.now())
                    .build();

            Notification savedNotification = notificationRepository.save(notification);
            return NotificationDTO.fromEntity(savedNotification);
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create notification", e);
        }
    }

    @Override
    public Page<NotificationDTO> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(NotificationDTO::fromEntity);
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream().map(NotificationDTO::fromEntity).toList();
    }

    @Override
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markNotificationsAsRead(List<Long> notificationIds) {
        if (!notificationIds.isEmpty()) {
            notificationRepository.markNotificationsAsRead(notificationIds, LocalDateTime.now());
            log.info("Marked {} notifications as read", notificationIds.size());
        }
    }

    @Override
    public void markAllNotificationsAsRead(Long userId) {
        notificationRepository.markAllNotificationsAsRead(userId, LocalDateTime.now());
        log.info("Marked all notifications as read for user: {}", userId);
    }

    @Override
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteOldReadNotifications(cutoffDate);
        log.info("Deleted notifications older than {} days", daysOld);
    }

    // System notification methods
    @Override
    public void notifyTaskAssigned(Long taskId, Long staffId) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            Staff staff = staffRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found"));

            String title = "New Task Assigned";
            String message = String.format("You have been assigned a new task: '%s'. Due date: %s",
                    task.getTitle(), task.getDueDate());

            createNotification(staff.getUser().getId(), title, message,
                    Notification.NotificationType.TASK_ASSIGNED, taskId, null);
        } catch (Exception e) {
            log.error("Error notifying task assignment: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyTaskStatusChanged(Long taskId, String oldStatus, String newStatus, Long updatedBy) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            // Notify the client
            String title = "Task Status Updated";
            String message = String.format("Your task '%s' status has been changed from %s to %s",
                    task.getTitle(), oldStatus, newStatus);

            Long clientUserId = task.getClient().getUser().getId();
            createNotification(clientUserId, title, message,
                    Notification.NotificationType.STATUS_UPDATE, taskId, null);

            // Notify assigned staff (if different from updater)
            if (task.getAssignedStaff() != null && !task.getAssignedStaff().getUser().getId().equals(updatedBy)) {
                String staffMessage = String.format("Task '%s' status has been updated to %s by another user",
                        task.getTitle(), newStatus);

                Long staffUserId = task.getAssignedStaff().getUser().getId();
                createNotification(staffUserId, title, staffMessage,
                        Notification.NotificationType.STATUS_UPDATE, taskId, null);
            }
        } catch (Exception e) {
            log.error("Error notifying task status change: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyDocumentUploaded(Long documentId, Long clientId) {
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // Notify the client
            String title = "Document Uploaded";
            String message = String.format("Your document '%s' has been successfully uploaded",
                    document.getOriginalFileName());

            createNotification(clientId, title, message,
                    Notification.NotificationType.DOCUMENT_UPLOADED,
                    document.getTask() != null ? document.getTask().getId() : null, documentId);

            // Notify assigned staff if task exists
            if (document.getTask() != null && document.getTask().getAssignedStaff() != null) {
                String staffMessage = String.format("New document '%s' uploaded for task '%s'",
                        document.getOriginalFileName(), document.getTask().getTitle());

                createNotification(document.getTask().getAssignedStaff().getUser().getId(), title, staffMessage,
                        Notification.NotificationType.DOCUMENT_UPLOADED, document.getTask().getId(), documentId);
            } else {
                // If no task is associated, try to find staff assigned to the client
                // and notify them about the document upload
                List<Task> clientTasks = taskRepository.findByClientId(document.getClient().getId());
                for (Task task : clientTasks) {
                    if (task.getAssignedStaff() != null) {
                        String staffMessage = String.format("New document '%s' uploaded by client '%s'",
                                document.getOriginalFileName(),
                                document.getClient().getCompanyName() != null ? document.getClient().getCompanyName()
                                        : document.getClient().getUser().getFirstName() + " "
                                                + document.getClient().getUser().getLastName());

                        createNotification(task.getAssignedStaff().getUser().getId(), title, staffMessage,
                                Notification.NotificationType.DOCUMENT_UPLOADED, null, documentId);
                        break; // Only notify one staff member to avoid duplicates
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error notifying document upload: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyDocumentUploadedByStaff(Long documentId, Long clientId, Long staffId) {
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            Staff staff = staffRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found"));

            // Notify the client that staff uploaded a document
            String title = "Document Uploaded by Staff";
            String message = String.format("Staff member %s %s has uploaded document '%s' for you",
                    staff.getUser().getFirstName(), staff.getUser().getLastName(),
                    document.getOriginalFileName());

            createNotification(clientId, title, message,
                    Notification.NotificationType.DOCUMENT_UPLOADED,
                    document.getTask() != null ? document.getTask().getId() : null, documentId);

            // Also notify other assigned staff if task exists and different from uploader
            if (document.getTask() != null && document.getTask().getAssignedStaff() != null
                    && !document.getTask().getAssignedStaff().getId().equals(staffId)) {
                String staffMessage = String.format("Document '%s' has been uploaded by %s %s for task '%s'",
                        document.getOriginalFileName(), staff.getUser().getFirstName(), staff.getUser().getLastName(),
                        document.getTask().getTitle());

                createNotification(document.getTask().getAssignedStaff().getUser().getId(), title, staffMessage,
                        Notification.NotificationType.DOCUMENT_UPLOADED, document.getTask().getId(), documentId);
            }
        } catch (Exception e) {
            log.error("Error notifying document upload by staff: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyDocumentVerified(Long documentId, Long verifiedBy) {
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            String title = "Document Verified";
            String message = String.format("Your document '%s' has been verified successfully",
                    document.getOriginalFileName());

            // Notify the client
            Long clientUserId = document.getClient().getUser().getId();
            createNotification(clientUserId, title, message,
                    Notification.NotificationType.DOCUMENT_VERIFIED,
                    document.getTask() != null ? document.getTask().getId() : null, documentId);

            // Notify assigned staff if task exists
            if (document.getTask() != null && document.getTask().getAssignedStaff() != null) {
                String staffMessage = String.format("Document '%s' has been verified for task '%s'",
                        document.getOriginalFileName(), document.getTask().getTitle());

                createNotification(document.getTask().getAssignedStaff().getUser().getId(), title, staffMessage,
                        Notification.NotificationType.DOCUMENT_VERIFIED, document.getTask().getId(), documentId);
            } else {
                // If no task is associated, try to find staff assigned to the client
                List<Task> clientTasks = taskRepository.findByClientId(document.getClient().getId());
                for (Task task : clientTasks) {
                    if (task.getAssignedStaff() != null) {
                        String staffMessage = String.format("Document '%s' has been verified for client '%s'",
                                document.getOriginalFileName(),
                                document.getClient().getCompanyName() != null ? document.getClient().getCompanyName()
                                        : document.getClient().getUser().getFirstName() + " "
                                                + document.getClient().getUser().getLastName());

                        createNotification(task.getAssignedStaff().getUser().getId(), title, staffMessage,
                                Notification.NotificationType.DOCUMENT_VERIFIED, null, documentId);
                        break; // Only notify one staff member to avoid duplicates
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error notifying document verification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyDocumentRejected(Long documentId, Long rejectedBy, String reason) {
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            String title = "Document Rejected";
            String message = String.format("Your document '%s' has been rejected. Reason: %s",
                    document.getOriginalFileName(), reason);

            // Notify the client
            createNotification(document.getClient().getUser().getId(), title, message,
                    Notification.NotificationType.DOCUMENT_REJECTED,
                    document.getTask() != null ? document.getTask().getId() : null, documentId);

            // Notify assigned staff if task exists
            if (document.getTask() != null && document.getTask().getAssignedStaff() != null) {
                String staffMessage = String.format("Document '%s' has been rejected for task '%s'. Reason: %s",
                        document.getOriginalFileName(), document.getTask().getTitle(), reason);

                createNotification(document.getTask().getAssignedStaff().getUser().getId(), title, staffMessage,
                        Notification.NotificationType.DOCUMENT_REJECTED, document.getTask().getId(), documentId);
            } else {
                // If no task is associated, try to find staff assigned to the client
                List<Task> clientTasks = taskRepository.findByClientId(document.getClient().getId());
                for (Task task : clientTasks) {
                    if (task.getAssignedStaff() != null) {
                        String staffMessage = String.format(
                                "Document '%s' has been rejected for client '%s'. Reason: %s",
                                document.getOriginalFileName(),
                                document.getClient().getCompanyName() != null ? document.getClient().getCompanyName()
                                        : document.getClient().getUser().getFirstName() + " "
                                                + document.getClient().getUser().getLastName(),
                                reason);

                        createNotification(task.getAssignedStaff().getUser().getId(), title, staffMessage,
                                Notification.NotificationType.DOCUMENT_REJECTED, null, documentId);
                        break; // Only notify one staff member to avoid duplicates
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error notifying document rejection: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyStaffAssignedToClient(Long clientId, Long staffId) {
        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            Staff staff = staffRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found"));

            // Notify the client
            String title = "Staff Assigned";
            String message = String.format("Staff member %s %s has been assigned to your account",
                    staff.getUser().getFirstName(), staff.getUser().getLastName());

            createNotification(client.getUser().getId(), title, message,
                    Notification.NotificationType.STAFF_ASSIGNED, null, null);

            // Notify the staff
            String staffMessage = String.format("You have been assigned to client: %s",
                    client.getCompanyName() != null ? client.getCompanyName()
                            : client.getUser().getFirstName() + " " + client.getUser().getLastName());

            createNotification(staff.getUser().getId(), title, staffMessage,
                    Notification.NotificationType.STAFF_ASSIGNED, null, null);
        } catch (Exception e) {
            log.error("Error notifying staff assignment: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyTaskCompleted(Long taskId, Long completedBy) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            String title = "Task Completed";
            String message = String.format("Your task '%s' has been completed successfully", task.getTitle());

            createNotification(task.getClient().getUser().getId(), title, message,
                    Notification.NotificationType.TASK_COMPLETED, taskId, null);
        } catch (Exception e) {
            log.error("Error notifying task completion: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyDeadlineReminder(Long taskId) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            String title = "Deadline Reminder";
            String message = String.format("Reminder: Task '%s' is due on %s",
                    task.getTitle(), task.getDueDate());

            // Notify client
            createNotification(task.getClient().getUser().getId(), title, message,
                    Notification.NotificationType.DEADLINE_REMINDER, taskId, null);

            // Notify assigned staff
            if (task.getAssignedStaff() != null) {
                createNotification(task.getAssignedStaff().getUser().getId(), title, message,
                        Notification.NotificationType.DEADLINE_REMINDER, taskId, null);
            }
        } catch (Exception e) {
            log.error("Error notifying deadline reminder: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyMessageReceived(Long fromUserId, Long toUserId, String message) {
        try {
            User fromUser = userRepository.findById(fromUserId)
                    .orElseThrow(() -> new RuntimeException("From user not found"));

            String title = "New Message";
            String messageText = String.format("You have received a new message from %s %s",
                    fromUser.getFirstName(), fromUser.getLastName());

            createNotification(toUserId, title, messageText,
                    Notification.NotificationType.MESSAGE_RECEIVED, null, null);
        } catch (Exception e) {
            log.error("Error notifying message received: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyLeadAssigned(Long leadId, Long staffId) {
        try {
            Staff staff = staffRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found"));

            User staffUser = staff.getUser();
            if (staffUser == null) {
                log.error("Staff user not found for staff ID: {}", staffId);
                return;
            }

            String title = "New Lead Assigned";
            String message = String
                    .format("A new lead has been assigned to you. Please review and take necessary action.");

            createNotification(staffUser.getId(), title, message, Notification.NotificationType.LEAD_ASSIGNED, null,
                    null);
        } catch (Exception e) {
            log.error("Error creating lead assignment notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyNewLeadCreated(Long leadId) {
        try {
            // This method can be used for any specific user notifications about new leads
            // For now, we'll leave it empty as it can be customized based on requirements
            log.info("New lead created notification placeholder for lead ID: {}", leadId);
        } catch (Exception e) {
            log.error("Error creating new lead notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyNewLeadCreatedForAdmin(Long leadId) {
        try {
            // Find all admin users
            List<User> adminUsers = userRepository.findByRole(User.Role.ADMIN);

            if (adminUsers.isEmpty()) {
                log.warn("No admin users found to notify about new lead");
                return;
            }

            String title = "New Lead Inquiry Received";
            String message = String.format(
                    "A new lead inquiry has been submitted. Lead ID: %s. Please review and assign to appropriate staff.",
                    leadId);

            // Send notification to all admin users
            for (User adminUser : adminUsers) {
                createNotification(adminUser.getId(), title, message, Notification.NotificationType.NEW_LEAD_CREATED,
                        null, null);
            }

            log.info("Created new lead notifications for {} admin users", adminUsers.size());
        } catch (Exception e) {
            log.error("Error creating new lead notification for admins: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyLeadConvertedToClient(Long leadId, Long clientId) {
        try {
            // Find all admin users
            List<User> adminUsers = userRepository.findByRole(User.Role.ADMIN);

            if (adminUsers.isEmpty()) {
                log.warn("No admin users found to notify about lead conversion");
                return;
            }

            String title = "Lead Successfully Converted to Client";
            String message = String.format(
                    "A lead has been successfully converted to a client. Lead ID: %s, Client ID: %s. The client account has been created and is ready for service.",
                    leadId, clientId);

            // Send notification to all admin users
            for (User adminUser : adminUsers) {
                createNotification(adminUser.getId(), title, message, Notification.NotificationType.LEAD_CONVERTED,
                        null, null);
            }

            log.info("Created lead conversion notifications for {} admin users", adminUsers.size());
        } catch (Exception e) {
            log.error("Error creating lead conversion notification for admins: {}", e.getMessage(), e);
        }
    }

    // Service Request notification methods
    @Override
    public void notifyServiceRequestCreated(Long serviceRequestId, Long createdByUserId) {
        try {
            // Find all admin users to notify about new service request
            List<User> adminUsers = userRepository.findByRole(User.Role.ADMIN);

            if (adminUsers.isEmpty()) {
                log.warn("No admin users found to notify about new service request");
                return;
            }

            String title = "New Service Request Created";
            String message = String.format(
                    "A new service request has been created. Service Request ID: %s. Please review and take necessary action.",
                    serviceRequestId);

            // Send notification to all admin users
            for (User adminUser : adminUsers) {
                createNotification(adminUser.getId(), title, message,
                        Notification.NotificationType.SERVICE_REQUEST_CREATED, null, null);
            }

            log.info("Created service request notifications for {} admin users", adminUsers.size());
        } catch (Exception e) {
            log.error("Error creating service request creation notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyServiceRequestStatusChanged(Long serviceRequestId, String oldStatus, String newStatus,
            Long updatedByUserId) {
        try {
            // Find the service request to get client information
            // Note: We'll need to inject ServiceRequestRepository or pass clientId as
            // parameter
            // For now, we'll implement the basic structure

            String title = "Service Request Status Updated";
            String message = String.format("Your service request status has been changed from %s to %s", oldStatus,
                    newStatus);

            // This method will be called from ServiceRequestServiceImpl where we have
            // access to client info
            // The actual client notification will be handled there

            log.info("Service request status change notification prepared for service request ID: {}",
                    serviceRequestId);
        } catch (Exception e) {
            log.error("Error preparing service request status change notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyServiceRequestAssigned(Long serviceRequestId, Long clientId, Long staffId) {
        try {
            // Notify the client about staff assignment
            String title = "Service Request Assigned";
            String message = "Your service request has been assigned to a staff member. We will begin working on it soon.";

            createNotification(clientId, title, message,
                    Notification.NotificationType.SERVICE_REQUEST_ASSIGNED, null, null);

            log.info("Created service request assignment notification for client ID: {}", clientId);
        } catch (Exception e) {
            log.error("Error creating service request assignment notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyServiceRequestRejected(Long serviceRequestId, Long clientId, Long rejectedByUserId) {
        try {
            // Notify the client about rejection
            String title = "Service Request Rejected";
            String message = "Your service request has been rejected. Please contact us for more information.";

            createNotification(clientId, title, message,
                    Notification.NotificationType.SERVICE_REQUEST_REJECTED, null, null);

            log.info("Created service request rejection notification for client ID: {}", clientId);
        } catch (Exception e) {
            log.error("Error creating service request rejection notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyServiceRequestCancelled(Long serviceRequestId, Long clientId, Long cancelledByUserId,
            Long assignedStaffId) {
        try {
            // Notify the client about cancellation
            String title = "Service Request Cancelled";
            String message = "Your service request has been cancelled.";

            createNotification(clientId, title, message,
                    Notification.NotificationType.SERVICE_REQUEST_CANCELLED, null, null);

            // If there's assigned staff, notify them about cancellation
            if (assignedStaffId != null) {
                String staffTitle = "Service Request Cancelled";
                String staffMessage = "A service request assigned to you has been cancelled.";

                createNotification(assignedStaffId, staffTitle, staffMessage,
                        Notification.NotificationType.SERVICE_REQUEST_CANCELLED, null, null);

                log.info("Created service request cancellation notification for staff ID: {}", assignedStaffId);
            }

            log.info("Created service request cancellation notification for client ID: {}", clientId);
        } catch (Exception e) {
            log.error("Error creating service request cancellation notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyServiceRequestConvertedToTask(Long serviceRequestId, Long clientId, Long taskId) {
        try {
            // Notify the client about conversion to task
            String title = "Service Request Converted to Task";
            String message = "Your service request has been converted to a task and is now being actively worked on.";

            createNotification(clientId, title, message,
                    Notification.NotificationType.SERVICE_REQUEST_CONVERTED_TO_TASK, taskId, null);

            // Find all admin users to notify about conversion
            List<User> adminUsers = userRepository.findByRole(User.Role.ADMIN);

            if (!adminUsers.isEmpty()) {
                String adminTitle = "Service Request Converted to Task";
                String adminMessage = String.format("Service request ID: %s has been converted to task ID: %s",
                        serviceRequestId, taskId);

                for (User adminUser : adminUsers) {
                    createNotification(adminUser.getId(), adminTitle, adminMessage,
                            Notification.NotificationType.SERVICE_REQUEST_CONVERTED_TO_TASK, taskId, null);
                }

                log.info("Created service request conversion notifications for {} admin users", adminUsers.size());
            }

            log.info("Created service request conversion notification for client ID: {}", clientId);
        } catch (Exception e) {
            log.error("Error creating service request conversion notification: {}", e.getMessage(), e);
        }
    }
}
