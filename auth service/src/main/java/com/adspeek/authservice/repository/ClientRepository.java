package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
        Optional<Client> findByUserId(Long userId);

        @Query("SELECT c FROM Client c JOIN c.user u WHERE " +
                        "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.gstNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.panNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<Client> findBySearchTerm(@Param("search") String search, Pageable pageable);

        @Query("SELECT c FROM Client c JOIN c.user u")
        Page<Client> findAllWithUser(Pageable pageable);

        @Query(value = """
                        SELECT
                            d.id as documentId,
                            d.document_name as documentName,
                            d.original_file_name as originalFileName,
                            d.upload_date_time as uploadDateTime,
                            d.file_type as fileType,
                            d.file_size as fileSize,
                            d.document_type as documentType,
                            d.status as status,
                            CONCAT(u.first_name, ' ', u.last_name) as uploadedByName,
                            u.role as uploadedByRole,
                            t.title as taskName
                        FROM documents d
                        JOIN users u ON d.uploaded_by_id = u.id
                        LEFT JOIN tasks t ON d.task_id = t.id
                        WHERE d.client_id = :clientId
                        ORDER BY d.upload_date_time DESC
                        """, nativeQuery = true)
        Page<Object[]> findClientDocuments(@Param("clientId") Long clientId, Pageable pageable);

        @Query(value = """
                        SELECT
                            t.id as taskId,
                            t.title as taskName,
                            t.description as description,
                            t.status as status,
                            t.priority as priority,
                            t.due_date as dueDate,
                            t.task_type as taskType,
                            CONCAT(su.first_name, ' ', su.last_name) as assignedStaffName,
                            s.employee_id as assignedStaffId,
                            t.created_at as createdAt,
                            t.updated_at as updatedAt
                        FROM tasks t
                        LEFT JOIN staff s ON t.assigned_staff_id = s.id
                        LEFT JOIN users su ON s.user_id = su.id
                        WHERE t.client_id = :clientId
                        ORDER BY t.due_date ASC
                        """, nativeQuery = true)
        Page<Object[]> findClientTasks(@Param("clientId") Long clientId, Pageable pageable);

        @Query(value = """
                        SELECT
                            COUNT(*) as totalClients,
                            SUM(CASE WHEN c.is_active = 1 THEN 1 ELSE 0 END) as activeClients,
                            SUM(CASE WHEN c.is_active = 0 THEN 1 ELSE 0 END) as inactiveClients,
                            COUNT(DISTINCT c.assigned_staff_id) as clientsWithAssignedStaff
                        FROM clients c
                        """, nativeQuery = true)
        Object[] getClientStats();

        @Query("SELECT c FROM Client c JOIN c.user u WHERE c.isActive = :isActive")
        Page<Client> findByStatus(@Param("isActive") Boolean isActive, Pageable pageable);

        @Query("SELECT c FROM Client c JOIN c.user u WHERE c.isActive = :isActive AND " +
                        "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Client> findBySearchTermAndStatus(@Param("search") String search, @Param("isActive") Boolean isActive,
                        Pageable pageable);

        // Client Dashboard specific methods
        @Query(value = "SELECT COUNT(*) FROM tasks t WHERE t.client_id = :clientId", nativeQuery = true)
        Long countTasksByClientId(@Param("clientId") Long clientId);

        @Query(value = "SELECT COUNT(*) FROM tasks t WHERE t.client_id = :clientId AND t.status = :status", nativeQuery = true)
        Long countTasksByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") String status);

        @Query(value = "SELECT COUNT(*) FROM tasks t WHERE t.client_id = :clientId AND t.due_date < CURDATE() AND t.status NOT IN ('COMPLETED', 'CANCELLED')", nativeQuery = true)
        Long countOverdueTasksByClientId(@Param("clientId") Long clientId);

        // Get clients by their IDs (for staff access)
        @Query("SELECT c FROM Client c JOIN c.user u WHERE c.id IN :clientIds")
        Page<Client> findByIds(@Param("clientIds") List<Long> clientIds, Pageable pageable);

        // Get clients by their IDs with search (for staff access)
        @Query("SELECT c FROM Client c JOIN c.user u WHERE c.id IN :clientIds AND " +
                        "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.gstNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.panNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Client> findByIdsAndSearchTerm(@Param("clientIds") List<Long> clientIds, @Param("search") String search,
                        Pageable pageable);

        // Get clients by their IDs with status filter (for staff access)
        @Query("SELECT c FROM Client c JOIN c.user u WHERE c.id IN :clientIds AND c.isActive = :isActive")
        Page<Client> findByIdsAndStatus(@Param("clientIds") List<Long> clientIds, @Param("isActive") Boolean isActive,
                        Pageable pageable);

        // Get clients by their IDs with search and status filter (for staff access)
        @Query("SELECT c FROM Client c JOIN c.user u WHERE c.id IN :clientIds AND c.isActive = :isActive AND " +
                        "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.gstNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.panNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Client> findByIdsAndSearchTermAndStatus(@Param("clientIds") List<Long> clientIds,
                        @Param("search") String search,
                        @Param("isActive") Boolean isActive, Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM documents d WHERE d.client_id = :clientId AND d.status = :status", nativeQuery = true)
        Long countDocumentsByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") String status);

        @Query(value = "SELECT COUNT(*) FROM tasks t WHERE t.client_id = :clientId AND t.due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND t.status NOT IN ('COMPLETED', 'CANCELLED')", nativeQuery = true)
        Long countUpcomingDeadlinesByClientId(@Param("clientId") Long clientId);

        @Query("SELECT c.id FROM Client c WHERE c.user.id = :userId")
        Long findClientIdByUserId(@Param("userId") Long userId);

        // Get client IDs assigned to a specific staff member
        @Query("SELECT c.id FROM Client c WHERE c.assignedStaff.id = :staffId")
        List<Long> findClientIdsByAssignedStaff(@Param("staffId") Long staffId);
}