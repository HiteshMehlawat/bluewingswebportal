package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
  Page<Staff> findAll(Pageable pageable);

  Optional<Staff> findByUserId(Long userId);

  @Query("SELECT s FROM Staff s JOIN FETCH s.user")
  Page<Staff> findAllWithUser(Pageable pageable);

  @Query("SELECT s FROM Staff s JOIN FETCH s.user WHERE " +
      "LOWER(s.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<Staff> findBySearchTerm(@Param("search") String search, Pageable pageable);

  @Query("SELECT s FROM Staff s JOIN FETCH s.user WHERE s.department = :department")
  Page<Staff> findByDepartment(@Param("department") String department, Pageable pageable);

  @Query("SELECT s FROM Staff s JOIN FETCH s.user WHERE s.isAvailable = :isAvailable")
  Page<Staff> findByStatus(@Param("isAvailable") Boolean isAvailable, Pageable pageable);

  @Query("SELECT s FROM Staff s JOIN FETCH s.user WHERE s.department = :department AND s.isAvailable = :isAvailable")
  Page<Staff> findByDepartmentAndStatus(@Param("department") String department,
      @Param("isAvailable") Boolean isAvailable, Pageable pageable);

  @Query("SELECT s FROM Staff s JOIN FETCH s.user WHERE " +
      "(LOWER(s.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
      "s.department = :department")
  Page<Staff> findBySearchTermAndDepartment(@Param("search") String search,
      @Param("department") String department,
      Pageable pageable);

  @Query("SELECT s FROM Staff s JOIN FETCH s.user WHERE " +
      "(LOWER(s.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
      "s.isAvailable = :isAvailable")
  Page<Staff> findBySearchTermAndStatus(@Param("search") String search, @Param("isAvailable") Boolean isAvailable,
      Pageable pageable);

  @Query("SELECT s FROM Staff s JOIN FETCH s.user WHERE " +
      "(LOWER(s.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(s.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
      "s.department = :department AND s.isAvailable = :isAvailable")
  Page<Staff> findBySearchTermAndDepartmentAndStatus(@Param("search") String search,
      @Param("department") String department, @Param("isAvailable") Boolean isAvailable,
      Pageable pageable);

  @Query("SELECT DISTINCT s.department FROM Staff s WHERE s.department IS NOT NULL ORDER BY s.department")
  List<String> findAllDepartments();

  @Query("SELECT s FROM Staff s JOIN FETCH s.user LEFT JOIN FETCH s.supervisor su LEFT JOIN FETCH su.user WHERE s.id = :id")
  Optional<Staff> findByIdWithSupervisor(@Param("id") Long id);

  @Query(value = """
      SELECT
        s.employee_id AS employeeId,
        CONCAT(u.first_name, ' ', u.last_name) AS name,
        s.position,
        s.department,
        s.supervisor_id AS supervisorId,
        COALESCE(COUNT(t.id), 0) AS totalAssigned,
        COALESCE(SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed,
        COALESCE(SUM(CASE WHEN t.status IN ('PENDING', 'IN_PROGRESS') THEN 1 ELSE 0 END), 0) AS pending,
        COALESCE(SUM(CASE WHEN t.status IN ('PENDING', 'IN_PROGRESS') AND t.due_date < CURDATE() THEN 1 ELSE 0 END), 0) AS overdue,
        COALESCE(COUNT(DISTINCT c.id), 0) AS totalAssignedClients,
        COALESCE(GREATEST(
          IFNULL(UNIX_TIMESTAMP(u.last_login), 0),
          IFNULL(MAX(UNIX_TIMESTAMP(t.updated_at)), 0)
        ), 0) AS lastActivityEpoch
      FROM staff s
      JOIN users u ON s.user_id = u.id
      LEFT JOIN tasks t ON t.assigned_staff_id = s.id
      LEFT JOIN clients c ON c.assigned_staff_id = s.id
      WHERE s.id = :staffId
      GROUP BY s.id, u.first_name, u.last_name, s.position, s.department, s.supervisor_id, s.employee_id
      """, nativeQuery = true)
  List<Object[]> findStaffPerformanceByIdNative(@Param("staffId") Long staffId);

  @Query(value = """
      SELECT
        s.employee_id AS employeeId,
        CONCAT(u.first_name, ' ', u.last_name) AS name,
        s.position,
        s.department,
        s.supervisor_id AS supervisorId,
        0 AS totalAssigned,
        0 AS completed,
        0 AS pending,
        0 AS overdue,
        COALESCE(UNIX_TIMESTAMP(u.last_login), 0) AS lastActivityEpoch
      FROM staff s
      JOIN users u ON s.user_id = u.id
      WHERE s.id = :staffId
      """, nativeQuery = true)
  Object[] findStaffBasicInfoByIdNative(@Param("staffId") Long staffId);

  @Query(value = """
      SELECT
        s.employee_id AS employeeId,
        CONCAT(u.first_name, ' ', u.last_name) AS name,
        s.position,
        s.department,
        s.supervisor_id AS supervisorId,
        COALESCE(COUNT(t.id), 0) AS totalAssigned,
        COALESCE(SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed,
        COALESCE(SUM(CASE WHEN t.status IN ('PENDING', 'IN_PROGRESS') THEN 1 ELSE 0 END), 0) AS pending,
        COALESCE(SUM(CASE WHEN t.status IN ('PENDING', 'IN_PROGRESS') AND t.due_date < CURDATE() THEN 1 ELSE 0 END), 0) AS overdue,
        COALESCE(GREATEST(
          IFNULL(UNIX_TIMESTAMP(u.last_login), 0),
          IFNULL(MAX(UNIX_TIMESTAMP(t.updated_at)), 0)
        ), 0) AS lastActivityEpoch
      FROM staff s
      JOIN users u ON s.user_id = u.id
      LEFT JOIN tasks t ON t.assigned_staff_id = s.id
      GROUP BY s.id, u.first_name, u.last_name, s.position, s.department, s.supervisor_id, s.employee_id
      """, countQuery = "SELECT COUNT(*) FROM staff", nativeQuery = true)
  Page<Object[]> findStaffPerformanceSummaryNative(Pageable pageable);

  @Query(value = """
      SELECT
        s.id as staff_id,
        s.employee_id,
        CONCAT(u.first_name, ' ', u.last_name) as staff_name,
        COUNT(t.id) as task_count,
        GROUP_CONCAT(t.title) as task_titles
      FROM staff s
      JOIN users u ON s.user_id = u.id
      LEFT JOIN tasks t ON t.assigned_staff_id = s.id
      GROUP BY s.id, s.employee_id, u.first_name, u.last_name
      """, nativeQuery = true)
  List<Object[]> findAllStaffWithTaskCounts();
}