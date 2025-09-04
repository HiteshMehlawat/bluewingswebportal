package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.ClientActivityDTO;
import com.adspeek.authservice.entity.AuditLog;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.repository.AuditLogRepository;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public AuditLog logActivity(String action, String entityType, Long entityId, String oldValues, String newValues,
            String ipAddress, String userAgent) {
        User currentUser = getCurrentUser();

        AuditLog auditLog = AuditLog.builder()
                .user(currentUser)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValues(oldValues)
                .newValues(newValues)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

        return auditLogRepository.save(auditLog);
    }

    @Override
    public AuditLog logActivityForCurrentUser(String action, String entityType, Long entityId, String oldValues,
            String newValues, String ipAddress, String userAgent) {
        return logActivity(action, entityType, entityId, oldValues, newValues, ipAddress, userAgent);
    }

    @Override
    public List<ClientActivityDTO> getRecentClientActivities(Long clientId, int limit) {
        List<Object[]> activities = auditLogRepository.findRecentActivitiesByClientIdWithLimit(clientId, limit);

        return activities.stream()
                .map(this::mapToClientActivityDTO)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ClientActivityDTO> getClientActivities(Long clientId, Pageable pageable) {
        Page<Object[]> activities = auditLogRepository.findRecentActivitiesByClientId(clientId, pageable);
        return activities.map(this::mapToClientActivityDTO);
    }

    @Override
    public Page<AuditLog> getActivitiesByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<AuditLog> getActivitiesByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }

    @Override
    public Page<AuditLog> getActivitiesByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return null;
    }

    private ClientActivityDTO mapToClientActivityDTO(Object[] row) {
        if (row == null || row.length < 13) {
            return null;
        }

        try {
            String action = (String) row[1];
            String entityType = (String) row[2];
            String relatedEntityName = (String) row[12];

            // Generate description based on action and entity type
            String description = generateActivityDescription(action, entityType, relatedEntityName);

            return ClientActivityDTO.builder()
                    .id(((Number) row[0]).longValue())
                    .activityType(action)
                    .description(description)
                    .details(generateActivityDetails(action, entityType, row[4], row[5])) // old_values, new_values
                    .activityDate(((java.sql.Timestamp) row[8]).toLocalDateTime())
                    .performedBy(row[10] != null ? (String) row[10] + " " + (String) row[11] : "System")
                    .performedByRole(row[12] != null ? (String) row[12] : "SYSTEM")
                    .relatedEntity(relatedEntityName)
                    .relatedEntityId(row[3] != null ? ((Number) row[3]).longValue() : null)
                    .ipAddress((String) row[6])
                    .userAgent((String) row[7])
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private String generateActivityDescription(String action, String entityType, String relatedEntityName) {
        switch (action) {
            case "DOCUMENT_UPLOADED":
                return "Document uploaded: " + (relatedEntityName != null ? relatedEntityName : "Unknown document");
            case "DOCUMENT_VERIFIED":
                return "Document verified: " + (relatedEntityName != null ? relatedEntityName : "Unknown document");
            case "DOCUMENT_REJECTED":
                return "Document rejected: " + (relatedEntityName != null ? relatedEntityName : "Unknown document");
            case "TASK_CREATED":
                return "Task created: " + (relatedEntityName != null ? relatedEntityName : "Unknown task");
            case "TASK_STATUS_UPDATED":
                return "Task status updated: " + (relatedEntityName != null ? relatedEntityName : "Unknown task");
            case "TASK_COMPLETED":
                return "Task completed: " + (relatedEntityName != null ? relatedEntityName : "Unknown task");
            case "PROFILE_UPDATED":
                return "Profile information updated";
            case "LOGIN_SUCCESSFUL":
                return "Successfully logged in";
            case "PASSWORD_CHANGED":
                return "Password changed";
            case "DOCUMENT_DOWNLOADED":
                return "Document downloaded: " + (relatedEntityName != null ? relatedEntityName : "Unknown document");
            default:
                return "Activity performed: " + action;
        }
    }

    private String generateActivityDetails(String action, String entityType, Object oldValues, Object newValues) {
        // This can be enhanced to provide more detailed information
        // For now, return a simple description
        switch (action) {
            case "TASK_STATUS_UPDATED":
                return "Task status was updated";
            case "PROFILE_UPDATED":
                return "Profile information was modified";
            case "DOCUMENT_VERIFIED":
                return "Document was verified by staff";
            case "DOCUMENT_REJECTED":
                return "Document was rejected with comments";
            default:
                return "";
        }
    }
}
