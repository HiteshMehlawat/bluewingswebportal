package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find recent activities for a specific client
     */
    @Query(value = """
            SELECT
                al.id, al.action, al.entity_type, al.entity_id, al.old_values, al.new_values,
                al.ip_address, al.user_agent, al.created_at,
                u.id as user_id, u.first_name, u.last_name, u.role,
                CASE
                    WHEN al.entity_type = 'DOCUMENT' THEN d.original_file_name
                    WHEN al.entity_type = 'TASK' THEN t.title
                    ELSE NULL
                END as related_entity_name
            FROM audit_logs al
            LEFT JOIN users u ON al.user_id = u.id
            LEFT JOIN documents d ON al.entity_type = 'DOCUMENT' AND al.entity_id = d.id
            LEFT JOIN tasks t ON al.entity_type = 'TASK' AND al.entity_id = t.id
            WHERE al.user_id IN (
                SELECT u2.id FROM users u2
                JOIN clients c ON u2.id = c.user_id
                WHERE c.id = :clientId
            )
            ORDER BY al.created_at DESC
            """, nativeQuery = true)
    Page<Object[]> findRecentActivitiesByClientId(@Param("clientId") Long clientId, Pageable pageable);

    /**
     * Find recent activities for a specific client with limit
     */
    @Query(value = """
            SELECT
                al.id, al.action, al.entity_type, al.entity_id, al.old_values, al.new_values,
                al.ip_address, al.user_agent, al.created_at,
                u.id as user_id, u.first_name, u.last_name, u.role,
                CASE
                    WHEN al.entity_type = 'DOCUMENT' THEN d.original_file_name
                    WHEN al.entity_type = 'TASK' THEN t.title
                    ELSE NULL
                END as related_entity_name
            FROM audit_logs al
            LEFT JOIN users u ON al.user_id = u.id
            LEFT JOIN documents d ON al.entity_type = 'DOCUMENT' AND al.entity_id = d.id
            LEFT JOIN tasks t ON al.entity_type = 'TASK' AND al.entity_id = t.id
            WHERE al.user_id IN (
                SELECT u2.id FROM users u2
                JOIN clients c ON u2.id = c.user_id
                WHERE c.id = :clientId
            )
            ORDER BY al.created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findRecentActivitiesByClientIdWithLimit(@Param("clientId") Long clientId, @Param("limit") int limit);

    /**
     * Find activities by user ID
     */
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find activities by action type
     */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    /**
     * Find activities by entity type and entity ID
     */
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);
}
