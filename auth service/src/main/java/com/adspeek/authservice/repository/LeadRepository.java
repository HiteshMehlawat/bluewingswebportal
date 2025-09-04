package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

        Optional<Lead> findByLeadId(String leadId);

        Optional<Lead> findByEmail(String email);

        List<Lead> findByStatus(Lead.Status status);

        @Query("SELECT l FROM Lead l WHERE l.assignedStaff.id = :staffId")
        List<Lead> findByAssignedStaffId(@Param("staffId") Long staffId);

        @Query("SELECT l FROM Lead l WHERE l.nextFollowUpDate = :date")
        List<Lead> findByNextFollowUpDate(@Param("date") LocalDate date);

        @Query("SELECT l FROM Lead l WHERE l.nextFollowUpDate <= :date AND l.status NOT IN ('CONVERTED', 'LOST')")
        List<Lead> findOverdueFollowUps(@Param("date") LocalDate date);

        @Query("SELECT COUNT(l) FROM Lead l WHERE l.status = :status")
        long countByStatus(@Param("status") Lead.Status status);

        @Query("SELECT COUNT(l) FROM Lead l WHERE l.assignedStaff.id = :staffId AND l.status = :status")
        long countByAssignedStaffIdAndStatus(@Param("staffId") Long staffId, @Param("status") Lead.Status status);

        @Query("SELECT l.source, COUNT(l) FROM Lead l GROUP BY l.source")
        List<Object[]> countBySource();

        @Query("SELECT l FROM Lead l ORDER BY l.createdAt DESC")
        Page<Lead> findRecentLeads(Pageable pageable);

        // Service hierarchy filter method for all leads
        @Query("SELECT l FROM Lead l WHERE " +
                        "(:search IS NULL OR l.firstName LIKE %:search% OR l.lastName LIKE %:search% OR l.email LIKE %:search% OR l.phone LIKE %:search%) AND "
                        +
                        "(:status IS NULL OR l.status = :status) AND " +
                        "(:priority IS NULL OR l.priority = :priority) AND " +
                        "(:serviceCategoryId IS NULL OR l.serviceItemId IN (SELECT si.id FROM ServiceItem si WHERE si.subcategory.id IN (SELECT sc.id FROM ServiceSubcategory sc WHERE sc.category.id = :serviceCategoryId))) AND "
                        +
                        "(:serviceSubcategoryId IS NULL OR l.serviceItemId IN (SELECT si.id FROM ServiceItem si WHERE si.subcategory.id = :serviceSubcategoryId)) AND "
                        +
                        "(:serviceItemId IS NULL OR l.serviceItemId = :serviceItemId)")
        Page<Lead> findByFiltersWithServiceHierarchy(
                        @Param("search") String search,
                        @Param("status") Lead.Status status,
                        @Param("priority") Lead.Priority priority,
                        @Param("serviceCategoryId") Long serviceCategoryId,
                        @Param("serviceSubcategoryId") Long serviceSubcategoryId,
                        @Param("serviceItemId") Long serviceItemId,
                        Pageable pageable);

        // Service hierarchy filter method for staff assigned leads
        @Query("SELECT l FROM Lead l WHERE l.assignedStaff.id = :staffId AND " +
                        "(:search IS NULL OR l.firstName LIKE %:search% OR l.lastName LIKE %:search% OR l.email LIKE %:search% OR l.phone LIKE %:search%) AND "
                        +
                        "(:status IS NULL OR l.status = :status) AND " +
                        "(:priority IS NULL OR l.priority = :priority) AND " +
                        "(:serviceCategoryId IS NULL OR l.serviceItemId IN (SELECT si.id FROM ServiceItem si WHERE si.subcategory.id IN (SELECT sc.id FROM ServiceSubcategory sc WHERE sc.category.id = :serviceCategoryId))) AND "
                        +
                        "(:serviceSubcategoryId IS NULL OR l.serviceItemId IN (SELECT si.id FROM ServiceItem si WHERE si.subcategory.id = :serviceSubcategoryId)) AND "
                        +
                        "(:serviceItemId IS NULL OR l.serviceItemId = :serviceItemId)")
        Page<Lead> findByAssignedStaffWithServiceHierarchyFilters(
                        @Param("staffId") Long staffId,
                        @Param("search") String search,
                        @Param("status") Lead.Status status,
                        @Param("priority") Lead.Priority priority,
                        @Param("serviceCategoryId") Long serviceCategoryId,
                        @Param("serviceSubcategoryId") Long serviceSubcategoryId,
                        @Param("serviceItemId") Long serviceItemId,
                        Pageable pageable);
}
