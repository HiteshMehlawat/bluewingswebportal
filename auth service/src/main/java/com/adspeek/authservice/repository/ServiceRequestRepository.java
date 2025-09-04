package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

        Optional<ServiceRequest> findByRequestId(String requestId);

        // Find by client
        Page<ServiceRequest> findByClientId(Long clientId, Pageable pageable);

        // Find by assigned staff
        Page<ServiceRequest> findByAssignedStaffId(Long staffId, Pageable pageable);

        // Find by status
        Page<ServiceRequest> findByStatus(ServiceRequest.Status status, Pageable pageable);

        // Find by client and status
        Page<ServiceRequest> findByClientIdAndStatus(Long clientId, ServiceRequest.Status status, Pageable pageable);

        // Find by assigned staff and status
        Page<ServiceRequest> findByAssignedStaffIdAndStatus(Long staffId, ServiceRequest.Status status,
                        Pageable pageable);

        // Find by service item
        Page<ServiceRequest> findByServiceItemId(Long serviceItemId, Pageable pageable);

        // Find by priority
        Page<ServiceRequest> findByPriority(ServiceRequest.Priority priority, Pageable pageable);

        // Find by created date range
        @Query("SELECT sr FROM ServiceRequest sr WHERE sr.createdAt BETWEEN :startDate AND :endDate")
        Page<ServiceRequest> findByCreatedDateBetween(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        // Find by preferred deadline range
        @Query("SELECT sr FROM ServiceRequest sr WHERE sr.preferredDeadline BETWEEN :startDate AND :endDate")
        Page<ServiceRequest> findByPreferredDeadlineBetween(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        // Search by request ID, client name, or company name
        @Query("SELECT sr FROM ServiceRequest sr JOIN sr.client c JOIN c.user u WHERE " +
                        "sr.requestId LIKE %:search% OR " +
                        "CONCAT(u.firstName, ' ', u.lastName) LIKE %:search% OR " +
                        "c.companyName LIKE %:search% OR " +
                        "sr.serviceCategoryName LIKE %:search% OR " +
                        "sr.serviceSubcategoryName LIKE %:search% OR " +
                        "sr.serviceItemName LIKE %:search%")
        Page<ServiceRequest> findBySearchTerm(@Param("search") String search, Pageable pageable);

        // Search with status filter
        @Query("SELECT sr FROM ServiceRequest sr JOIN sr.client c JOIN c.user u WHERE " +
                        "sr.status = :status AND (" +
                        "sr.requestId LIKE %:search% OR " +
                        "CONCAT(u.firstName, ' ', u.lastName) LIKE %:search% OR " +
                        "c.companyName LIKE %:search% OR " +
                        "sr.serviceCategoryName LIKE %:search% OR " +
                        "sr.serviceSubcategoryName LIKE %:search% OR " +
                        "sr.serviceItemName LIKE %:search%)")
        Page<ServiceRequest> findBySearchTermAndStatus(@Param("search") String search,
                        @Param("status") ServiceRequest.Status status,
                        Pageable pageable);

        // Find by client IDs (for staff access)
        @Query("SELECT sr FROM ServiceRequest sr JOIN sr.client c JOIN c.user u WHERE sr.client.id IN :clientIds")
        Page<ServiceRequest> findByClientIds(@Param("clientIds") List<Long> clientIds, Pageable pageable);

        // Find by client IDs with search
        @Query("SELECT sr FROM ServiceRequest sr JOIN sr.client c JOIN c.user u WHERE sr.client.id IN :clientIds AND ("
                        +
                        "sr.requestId LIKE %:search% OR " +
                        "CONCAT(u.firstName, ' ', u.lastName) LIKE %:search% OR " +
                        "c.companyName LIKE %:search% OR " +
                        "sr.serviceCategoryName LIKE %:search% OR " +
                        "sr.serviceSubcategoryName LIKE %:search% OR " +
                        "sr.serviceItemName LIKE %:search%)")
        Page<ServiceRequest> findByClientIdsAndSearchTerm(@Param("clientIds") List<Long> clientIds,
                        @Param("search") String search,
                        Pageable pageable);

        // Find by client IDs with status
        @Query("SELECT sr FROM ServiceRequest sr JOIN sr.client c JOIN c.user u WHERE sr.client.id IN :clientIds AND sr.status = :status")
        Page<ServiceRequest> findByClientIdsAndStatus(@Param("clientIds") List<Long> clientIds,
                        @Param("status") ServiceRequest.Status status,
                        Pageable pageable);

        // Find by client IDs with search and status
        @Query("SELECT sr FROM ServiceRequest sr JOIN sr.client c JOIN c.user u WHERE sr.client.id IN :clientIds AND sr.status = :status AND ("
                        +
                        "sr.requestId LIKE %:search% OR " +
                        "CONCAT(u.firstName, ' ', u.lastName) LIKE %:search% OR " +
                        "c.companyName LIKE %:search% OR " +
                        "sr.serviceCategoryName LIKE %:search% OR " +
                        "sr.serviceSubcategoryName LIKE %:search% OR " +
                        "sr.serviceItemName LIKE %:search%)")
        Page<ServiceRequest> findByClientIdsAndSearchTermAndStatus(@Param("clientIds") List<Long> clientIds,
                        @Param("search") String search,
                        @Param("status") ServiceRequest.Status status,
                        Pageable pageable);

        // Count by status
        @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.status = :status")
        Long countByStatus(@Param("status") ServiceRequest.Status status);

        // Count by client and status
        @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.client.id = :clientId AND (:status IS NULL OR sr.status = :status)")
        Long countByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") ServiceRequest.Status status);

        // Count by assigned staff and status
        @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.assignedStaff.id = :staffId AND (:status IS NULL OR sr.status = :status)")
        Long countByAssignedStaffIdAndStatus(@Param("staffId") Long staffId,
                        @Param("status") ServiceRequest.Status status);

        // Find overdue requests (preferred deadline passed and status not
        // completed/cancelled/rejected)
        @Query("SELECT sr FROM ServiceRequest sr WHERE sr.preferredDeadline < :today AND sr.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED')")
        Page<ServiceRequest> findOverdueRequests(@Param("today") LocalDate today, Pageable pageable);

        // Find requests due soon (within 7 days)
        @Query("SELECT sr FROM ServiceRequest sr WHERE sr.preferredDeadline BETWEEN :today AND :weekFromNow AND sr.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED')")
        Page<ServiceRequest> findRequestsDueSoon(@Param("today") LocalDate today,
                        @Param("weekFromNow") LocalDate weekFromNow,
                        Pageable pageable);

        // Find all with filters (for admin)
        @Query("SELECT sr FROM ServiceRequest sr JOIN sr.client c JOIN c.user u WHERE " +
                        "(:search IS NULL OR :search = '' OR " +
                        "sr.requestId LIKE %:search% OR " +
                        "CONCAT(u.firstName, ' ', u.lastName) LIKE %:search% OR " +
                        "c.companyName LIKE %:search% OR " +
                        "sr.serviceCategoryName LIKE %:search% OR " +
                        "sr.serviceSubcategoryName LIKE %:search% OR " +
                        "sr.serviceItemName LIKE %:search%) AND " +
                        "(:statusFilter IS NULL OR :statusFilter = '' OR sr.status = :statusFilter) AND " +
                        "(:priorityFilter IS NULL OR :priorityFilter = '' OR sr.priority = :priorityFilter)")
        Page<ServiceRequest> findAllWithFilters(@Param("search") String search,
                        @Param("statusFilter") String statusFilter,
                        @Param("priorityFilter") String priorityFilter,
                        Pageable pageable);
}
