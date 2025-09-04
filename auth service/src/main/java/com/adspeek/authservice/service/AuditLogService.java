package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.ClientActivityDTO;
import com.adspeek.authservice.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {

    /**
     * Log an activity
     */
    AuditLog logActivity(String action, String entityType, Long entityId, String oldValues, String newValues,
            String ipAddress, String userAgent);

    /**
     * Log an activity for the current user
     */
    AuditLog logActivityForCurrentUser(String action, String entityType, Long entityId, String oldValues,
            String newValues, String ipAddress, String userAgent);

    /**
     * Get recent activities for a client
     */
    List<ClientActivityDTO> getRecentClientActivities(Long clientId, int limit);

    /**
     * Get paginated activities for a client
     */
    Page<ClientActivityDTO> getClientActivities(Long clientId, Pageable pageable);

    /**
     * Get activities by user ID
     */
    Page<AuditLog> getActivitiesByUserId(Long userId, Pageable pageable);

    /**
     * Get activities by action type
     */
    Page<AuditLog> getActivitiesByAction(String action, Pageable pageable);

    /**
     * Get activities by entity type and entity ID
     */
    Page<AuditLog> getActivitiesByEntity(String entityType, Long entityId, Pageable pageable);
}
