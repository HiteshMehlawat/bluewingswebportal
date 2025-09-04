package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
  long countByStatus(String status);

  List<Task> findByClientId(Long clientId);

  // Enhanced queries for task management
  @Query(value = """
      SELECT
        t.id, t.title, t.description, t.client_id, t.assigned_staff_id,
        t.task_type, t.status, t.priority, t.due_date, t.assigned_date,
        t.started_date, t.completed_date, t.estimated_hours, t.actual_hours,
        t.created_by, t.updated_by, t.created_at, t.updated_at,
        COALESCE(CONCAT(c.first_name, ' ', c.last_name), 'Unknown') as client_name,
        COALESCE(CONCAT(s.first_name, ' ', s.last_name), 'Unassigned') as staff_name,
        COALESCE(st.employee_id, 'N/A') as employee_id,
        COALESCE(CONCAT(creator.first_name, ' ', creator.last_name), 'System') as created_by_name,
        COALESCE(creator.email, 'system@example.com') as created_by_email,
        COALESCE(CONCAT(updater.first_name, ' ', updater.last_name), 'System') as updated_by_name
      FROM tasks t
      LEFT JOIN clients cl ON t.client_id = cl.id
      LEFT JOIN users c ON cl.user_id = c.id
      LEFT JOIN staff st ON t.assigned_staff_id = st.id
      LEFT JOIN users s ON st.user_id = s.id
      LEFT JOIN users creator ON t.created_by = creator.id
      LEFT JOIN users updater ON t.updated_by = updater.id
      WHERE (:status IS NULL OR t.status = :status) AND
            (:taskType IS NULL OR t.task_type = :taskType) AND
            (:priority IS NULL OR t.priority = :priority) AND
            (:assignedStaffId IS NULL OR t.assigned_staff_id = :assignedStaffId) AND
            (:clientId IS NULL OR t.client_id = :clientId) AND
            (:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      """, countQuery = """
      SELECT COUNT(*) FROM tasks t
      WHERE (:status IS NULL OR t.status = :status) AND
            (:taskType IS NULL OR t.task_type = :taskType) AND
            (:priority IS NULL OR t.priority = :priority) AND
            (:assignedStaffId IS NULL OR t.assigned_staff_id = :assignedStaffId) AND
            (:clientId IS NULL OR t.client_id = :clientId) AND
            (:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      """, nativeQuery = true)
  Page<Object[]> findByFilters(
      @Param("status") String status,
      @Param("taskType") String taskType,
      @Param("priority") String priority,
      @Param("assignedStaffId") Long assignedStaffId,
      @Param("clientId") Long clientId,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  // Enhanced query with service hierarchy support
  @Query(value = """
      SELECT
        t.id, t.title, t.description, t.client_id, t.assigned_staff_id,
        t.task_type, t.status, t.priority, t.due_date, t.assigned_date,
        t.started_date, t.completed_date, t.estimated_hours, t.actual_hours,
        t.created_by, t.updated_by, t.created_at, t.updated_at,
        COALESCE(CONCAT(c.first_name, ' ', c.last_name), 'Unknown') as client_name,
        COALESCE(CONCAT(s.first_name, ' ', s.last_name), 'Unassigned') as staff_name,
        COALESCE(st.employee_id, 'N/A') as employee_id,
        COALESCE(CONCAT(creator.first_name, ' ', creator.last_name), 'System') as created_by_name,
        COALESCE(creator.email, 'system@example.com') as created_by_email,
        COALESCE(CONCAT(updater.first_name, ' ', updater.last_name), 'System') as updated_by_name
      FROM tasks t
      LEFT JOIN clients cl ON t.client_id = cl.id
      LEFT JOIN users c ON cl.user_id = c.id
      LEFT JOIN staff st ON t.assigned_staff_id = st.id
      LEFT JOIN users s ON st.user_id = s.id
      LEFT JOIN users creator ON t.created_by = creator.id
      LEFT JOIN users updater ON t.updated_by = updater.id
      LEFT JOIN service_items si ON t.service_item_id = si.id
      LEFT JOIN service_subcategories ssc ON si.subcategory_id = ssc.id
      LEFT JOIN service_categories sc ON ssc.category_id = sc.id
      WHERE (:status IS NULL OR t.status = :status) AND
            (:priority IS NULL OR t.priority = :priority) AND
            (:assignedStaffId IS NULL OR t.assigned_staff_id = :assignedStaffId) AND
            (:clientId IS NULL OR t.client_id = :clientId) AND
            (:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND
            (:serviceCategoryId IS NULL OR sc.id = :serviceCategoryId) AND
            (:serviceSubcategoryId IS NULL OR ssc.id = :serviceSubcategoryId) AND
            (:serviceItemId IS NULL OR si.id = :serviceItemId)
      ORDER BY t.created_at DESC
      """, countQuery = """
      SELECT COUNT(*) FROM tasks t
      LEFT JOIN service_items si ON t.service_item_id = si.id
      LEFT JOIN service_subcategories ssc ON si.subcategory_id = ssc.id
      LEFT JOIN service_categories sc ON ssc.category_id = sc.id
      WHERE (:status IS NULL OR t.status = :status) AND
            (:priority IS NULL OR t.priority = :priority) AND
            (:assignedStaffId IS NULL OR t.assigned_staff_id = :assignedStaffId) AND
            (:clientId IS NULL OR t.client_id = :clientId) AND
            (:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND
            (:serviceCategoryId IS NULL OR sc.id = :serviceCategoryId) AND
            (:serviceSubcategoryId IS NULL OR ssc.id = :serviceSubcategoryId) AND
            (:serviceItemId IS NULL OR si.id = :serviceItemId)
      """, nativeQuery = true)
  Page<Object[]> findByFiltersWithServiceHierarchy(
      @Param("status") String status,
      @Param("priority") String priority,
      @Param("assignedStaffId") Long assignedStaffId,
      @Param("clientId") Long clientId,
      @Param("searchTerm") String searchTerm,
      @Param("serviceCategoryId") Long serviceCategoryId,
      @Param("serviceSubcategoryId") Long serviceSubcategoryId,
      @Param("serviceItemId") Long serviceItemId,
      Pageable pageable);

  // Search tasks by title or description

  @Query(value = """
      SELECT
        t.id, t.title, t.description, t.client_id, t.assigned_staff_id,
        t.task_type, t.status, t.priority, t.due_date, t.assigned_date,
        t.started_date, t.completed_date, t.estimated_hours, t.actual_hours,
        t.created_by, t.updated_by, t.created_at, t.updated_at,
        CONCAT(c.first_name, ' ', c.last_name) as client_name,
        CONCAT(s.first_name, ' ', s.last_name) as staff_name,
        st.employee_id as employee_id,
        CONCAT(creator.first_name, ' ', creator.last_name) as created_by_name,
        creator.email as created_by_email,
        CONCAT(updater.first_name, ' ', updater.last_name) as updated_by_name
      FROM tasks t
      LEFT JOIN clients cl ON t.client_id = cl.id
      LEFT JOIN users c ON cl.user_id = c.id
      LEFT JOIN staff st ON t.assigned_staff_id = st.id
      LEFT JOIN users s ON st.user_id = s.id
      LEFT JOIN users creator ON t.created_by = creator.id
      LEFT JOIN users updater ON t.updated_by = updater.id
      WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
         OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
      ORDER BY t.created_at DESC
      """, nativeQuery = true)
  Page<Object[]> findBySearchTermNative(@Param("searchTerm") String searchTerm, Pageable pageable);

  // Get overdue tasks
  @Query(value = "SELECT t.* FROM tasks t WHERE t.due_date < :today AND t.status != 'COMPLETED'", nativeQuery = true)
  List<Task> findOverdueTasks(@Param("today") LocalDate today);

  // Get overdue tasks with LocalDateTime
  @Query(value = "SELECT t.* FROM tasks t WHERE t.due_date < :now AND t.status != 'COMPLETED'", nativeQuery = true)
  List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

  // Get tasks due soon (within 3 days)
  @Query(value = "SELECT t.* FROM tasks t WHERE t.due_date BETWEEN :today AND :threeDaysLater AND t.status != 'COMPLETED'", nativeQuery = true)
  List<Task> findTasksDueSoon(@Param("today") LocalDate today, @Param("threeDaysLater") LocalDate threeDaysLater);

  // Get tasks due within 24 hours
  @Query(value = "SELECT t.* FROM tasks t WHERE t.due_date BETWEEN :now AND :tomorrow AND t.status != 'COMPLETED'", nativeQuery = true)
  List<Task> findTasksDueWithin24Hours(@Param("now") LocalDateTime now, @Param("tomorrow") LocalDateTime tomorrow);

  // Get tasks by staff member
  @Query(value = "SELECT t.* FROM tasks t WHERE t.assigned_staff_id = :staffId ORDER BY t.created_at DESC", countQuery = "SELECT COUNT(*) FROM tasks t WHERE t.assigned_staff_id = :staffId", nativeQuery = true)
  Page<Task> findByAssignedStaffId(@Param("staffId") Long staffId, Pageable pageable);

  // Get tasks by staff member and client
  @Query(value = "SELECT t.* FROM tasks t WHERE t.assigned_staff_id = :staffId AND t.client_id = :clientId ORDER BY t.created_at DESC", countQuery = "SELECT COUNT(*) FROM tasks t WHERE t.assigned_staff_id = :staffId AND t.client_id = :clientId", nativeQuery = true)
  Page<Task> findByAssignedStaffIdAndClientId(@Param("staffId") Long staffId, @Param("clientId") Long clientId,
      Pageable pageable);

  // Get task statistics
  @Query(value = """
      SELECT
        COALESCE(COUNT(*), 0) as totalTasks,
        COALESCE(SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END), 0) as pendingTasks,
        COALESCE(SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) as inProgressTasks,
        COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) as completedTasks,
        COALESCE(SUM(CASE WHEN status = 'ON_HOLD' THEN 1 ELSE 0 END), 0) as onHoldTasks,
        COALESCE(SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END), 0) as cancelledTasks,
        COALESCE(SUM(CASE WHEN due_date < :today AND status != 'COMPLETED' THEN 1 ELSE 0 END), 0) as overdueTasks
      FROM tasks
      """, nativeQuery = true)
  List<Object[]> getTaskStatistics(@Param("today") LocalDate today);

  // Get task statistics for a specific staff member
  @Query(value = """
      SELECT
        COALESCE(COUNT(*), 0) as totalTasks,
        COALESCE(SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END), 0) as pendingTasks,
        COALESCE(SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) as inProgressTasks,
        COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) as completedTasks,
        COALESCE(SUM(CASE WHEN status = 'ON_HOLD' THEN 1 ELSE 0 END), 0) as onHoldTasks,
        COALESCE(SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END), 0) as cancelledTasks,
        COALESCE(SUM(CASE WHEN due_date < :today AND status != 'COMPLETED' THEN 1 ELSE 0 END), 0) as overdueTasks
      FROM tasks
      WHERE assigned_staff_id = :staffId
      """, nativeQuery = true)
  List<Object[]> getTaskStatisticsByStaff(@Param("staffId") Long staffId, @Param("today") LocalDate today);

  // Get staff workload summary
  @Query(value = """
      SELECT
        s.id as staffId,
        CONCAT(u.first_name, ' ', u.last_name) as staffName,
        s.position,
        s.department,
        COALESCE(COUNT(t.id), 0) as totalTasks,
        COALESCE(SUM(CASE WHEN t.status = 'PENDING' THEN 1 ELSE 0 END), 0) as pendingTasks,
        COALESCE(SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) as inProgressTasks,
        COALESCE(SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) as completedTasks,
        COALESCE(SUM(CASE WHEN t.due_date < :today AND t.status != 'COMPLETED' THEN 1 ELSE 0 END), 0) as overdueTasks
      FROM staff s
      JOIN users u ON s.user_id = u.id
      LEFT JOIN tasks t ON t.assigned_staff_id = s.id
      WHERE s.is_available = true
      GROUP BY s.id, u.first_name, u.last_name, s.position, s.department
      ORDER BY totalTasks DESC
      """, nativeQuery = true)
  List<Object[]> getStaffWorkloadSummary(@Param("today") LocalDate today);

  // Get task timeline for a specific task
  @Query(value = """
      SELECT
        t.id, t.title, t.status, t.priority, t.due_date,
        t.assigned_date, t.started_date, t.completed_date,
        CONCAT(c.first_name, ' ', c.last_name) as clientName,
        CONCAT(s.first_name, ' ', s.last_name) as staffName,
        CONCAT(creator.first_name, ' ', creator.last_name) as createdByName
      FROM tasks t
      LEFT JOIN clients cl ON t.client_id = cl.id
      LEFT JOIN users c ON cl.user_id = c.id
      LEFT JOIN staff st ON t.assigned_staff_id = st.id
      LEFT JOIN users s ON st.user_id = s.id
      LEFT JOIN users creator ON t.created_by = creator.id
      WHERE t.id = :taskId
      """, nativeQuery = true)
  Object[] getTaskDetails(@Param("taskId") Long taskId);

  // Get available staff for task assignment (with least workload)
  @Query(value = """
      SELECT
        s.id as staffId,
        CONCAT(u.first_name, ' ', u.last_name) as staffName,
        s.employee_id as employeeId,
        s.position,
        s.department,
        COALESCE(COUNT(t.id), 0) as currentTasks
      FROM staff s
      JOIN users u ON s.user_id = u.id
      LEFT JOIN tasks t ON t.assigned_staff_id = s.id AND t.status IN ('PENDING', 'IN_PROGRESS')
      WHERE s.is_available = true
      GROUP BY s.id, u.first_name, u.last_name, s.employee_id, s.position, s.department
      ORDER BY currentTasks ASC
      """, nativeQuery = true)
  List<Object[]> getAvailableStaffForAssignment();

  @Query(value = """
      SELECT
        t.id,
        t.title,
        t.description,
        t.due_date,
        t.status,
        t.priority,
        t.task_type,
        s.id as staff_id,
        CONCAT(u.first_name, ' ', u.last_name) as staff_name,
        (SELECT comment FROM task_comments WHERE task_id = t.id ORDER BY created_at DESC LIMIT 1) as latest_remark,
        CASE
          WHEN t.status != 'COMPLETED' AND t.due_date < CURDATE() THEN 'Overdue'
          WHEN t.status != 'COMPLETED' AND t.due_date <= DATE_ADD(CURDATE(), INTERVAL 3 DAY) THEN 'Due Soon'
          ELSE 'Safe'
        END as deadline_status,
        EXISTS (SELECT 1 FROM documents d WHERE d.task_id = t.id) as has_documents
      FROM tasks t
      LEFT JOIN staff s ON t.assigned_staff_id = s.id
      LEFT JOIN users u ON s.user_id = u.id
      WHERE t.client_id = :clientId
        AND t.status IN ('PENDING', 'IN_PROGRESS')
        AND t.due_date >= CURDATE()
      ORDER BY t.due_date
      """, countQuery = "SELECT COUNT(*) FROM tasks t WHERE t.client_id = :clientId AND t.status IN ('PENDING', 'IN_PROGRESS') AND t.due_date >= CURDATE()", nativeQuery = true)
  Page<Object[]> findUpcomingDeadlinesByClient(@Param("clientId") Long clientId, Pageable pageable);

  // Deadline-related methods
  @Query(value = """
      SELECT
        t.id, t.title, t.description, t.client_id, t.assigned_staff_id,
        t.task_type, t.status, t.priority, t.due_date, t.assigned_date,
        t.started_date, t.completed_date, t.estimated_hours, t.actual_hours,
        t.created_by, t.updated_by, t.created_at, t.updated_at,
        CONCAT(c.first_name, ' ', c.last_name) as client_name,
        CONCAT(s.first_name, ' ', s.last_name) as staff_name,
        st.employee_id as employee_id,
        CONCAT(creator.first_name, ' ', creator.last_name) as created_by_name,
        CONCAT(updater.first_name, ' ', updater.last_name) as updated_by_name
      FROM tasks t
      LEFT JOIN clients cl ON t.client_id = cl.id
      LEFT JOIN users c ON cl.user_id = c.id
      LEFT JOIN staff st ON t.assigned_staff_id = st.id
      LEFT JOIN users s ON st.user_id = s.id
      LEFT JOIN users creator ON t.created_by = creator.id
      LEFT JOIN users updater ON t.updated_by = updater.id
      WHERE (:clientId IS NULL OR t.client_id = :clientId)
      AND (:status IS NULL OR t.status = :status)
      AND (:priority IS NULL OR t.priority = :priority)
      AND (:taskType IS NULL OR t.task_type = :taskType)
      AND (:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      """, countQuery = """
      SELECT COUNT(*) FROM tasks t
      WHERE (:clientId IS NULL OR t.client_id = :clientId)
      AND (:status IS NULL OR t.status = :status)
      AND (:priority IS NULL OR t.priority = :priority)
      AND (:taskType IS NULL OR t.task_type = :taskType)
      AND (:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      """, nativeQuery = true)
  Page<Object[]> findTasksWithDeadlineInfo(
      @Param("clientId") Long clientId,
      @Param("status") String status,
      @Param("priority") String priority,
      @Param("taskType") String taskType,
      @Param("isOverdue") Boolean isOverdue,
      @Param("searchTerm") String searchTerm,
      @Param("today") LocalDateTime today,
      Pageable pageable);

  @Query(value = "SELECT t.* FROM tasks t WHERE t.due_date BETWEEN :startDate AND :endDate", nativeQuery = true)
  List<Task> findTasksByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @Query(value = "SELECT t.* FROM tasks t WHERE t.due_date >= :today ORDER BY t.due_date ASC", nativeQuery = true)
  List<Task> findUpcomingTasks(@Param("today") LocalDate today);

  @Query(value = "SELECT COUNT(*) FROM tasks t", nativeQuery = true)
  Long countAllTasks();

  @Query(value = "SELECT COUNT(*) FROM tasks t WHERE t.due_date < :today AND t.status != 'COMPLETED'", nativeQuery = true)
  Long countOverdueTasks(@Param("today") LocalDate today);

  @Query(value = "SELECT COUNT(*) FROM tasks t WHERE t.due_date BETWEEN :startDate AND :endDate AND t.status != 'COMPLETED'", nativeQuery = true)
  Long countDueSoonTasks(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  // Calculate average completion time for a specific staff member
  @Query(value = """
      SELECT COALESCE(AVG(TIMESTAMPDIFF(HOUR, t.started_date, t.completed_date)), 0) as avgCompletionTime
      FROM tasks t
      WHERE t.assigned_staff_id = :staffId
      AND t.status = 'COMPLETED'
      AND t.started_date IS NOT NULL
      AND t.completed_date IS NOT NULL
      AND t.completed_date > t.started_date
      """, nativeQuery = true)
  Double getAverageCompletionTimeByStaff(@Param("staffId") Long staffId);

  // Count unique clients assigned to a staff member through tasks
  @Query(value = """
      SELECT COALESCE(COUNT(DISTINCT t.client_id), 0) as uniqueClients
      FROM tasks t
      WHERE t.assigned_staff_id = :staffId
      """, nativeQuery = true)
  Long getUniqueClientsCountByStaff(@Param("staffId") Long staffId);

  // Get unique client IDs assigned to a staff member through tasks
  @Query(value = """
      SELECT DISTINCT t.client_id
      FROM tasks t
      WHERE t.assigned_staff_id = :staffId
      """, nativeQuery = true)
  List<Long> getUniqueClientIdsByStaff(@Param("staffId") Long staffId);

  // Note: TaskComment entity doesn't exist, so we'll return null for comments
  // Note: Document task relationship might not exist, so we'll return false for
  // hasDocuments

  // Client Dashboard specific methods
  @Query(value = "SELECT t.* FROM tasks t WHERE t.client_id = :clientId", countQuery = "SELECT COUNT(*) FROM tasks t WHERE t.client_id = :clientId", nativeQuery = true)
  Page<Task> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

  // Client Dashboard detailed methods with staff contact info
  @Query(value = """
      SELECT
        t.id, t.title, t.description, t.client_id, t.assigned_staff_id,
        t.task_type, t.status, t.priority, t.due_date, t.assigned_date,
        t.started_date, t.completed_date, t.estimated_hours, t.actual_hours,
        t.created_by, t.updated_by, t.created_at, t.updated_at,
        CONCAT(c.first_name, ' ', c.last_name) as client_name,
        c.phone as client_phone,
        c.email as client_email,
        CONCAT(s.first_name, ' ', s.last_name) as staff_name,
        st.employee_id as employee_id,
        s.phone as staff_phone,
        s.email as staff_email,
        CONCAT(creator.first_name, ' ', creator.last_name) as created_by_name,
        creator.email as created_by_email,
        CONCAT(updater.first_name, ' ', updater.last_name) as updated_by_name
      FROM tasks t
      LEFT JOIN clients cl ON t.client_id = cl.id
      LEFT JOIN users c ON cl.user_id = c.id
      LEFT JOIN staff st ON t.assigned_staff_id = st.id
      LEFT JOIN users s ON st.user_id = s.id
      LEFT JOIN users creator ON t.created_by = creator.id
      LEFT JOIN users updater ON t.updated_by = updater.id
      WHERE t.client_id = :clientId
      """, countQuery = "SELECT COUNT(*) FROM tasks t WHERE t.client_id = :clientId", nativeQuery = true)
  Page<Object[]> findTasksByClientWithDetails(@Param("clientId") Long clientId, Pageable pageable);

  @Query(value = """
      SELECT
        t.id, t.title, t.description, t.client_id, t.assigned_staff_id,
        t.task_type, t.status, t.priority, t.due_date, t.assigned_date,
        t.started_date, t.completed_date, t.estimated_hours, t.actual_hours,
        t.created_by, t.updated_by, t.created_at, t.updated_at,
        CONCAT(c.first_name, ' ', c.last_name) as client_name,
        c.phone as client_phone,
        c.email as client_email,
        CONCAT(s.first_name, ' ', s.last_name) as staff_name,
        st.employee_id as employee_id,
        s.phone as staff_phone,
        s.email as staff_email,
        CONCAT(creator.first_name, ' ', creator.last_name) as created_by_name,
        creator.email as created_by_email,
        CONCAT(updater.first_name, ' ', updater.last_name) as updated_by_name
      FROM tasks t
      LEFT JOIN clients cl ON t.client_id = cl.id
      LEFT JOIN users c ON cl.user_id = c.id
      LEFT JOIN staff st ON t.assigned_staff_id = st.id
      LEFT JOIN users s ON st.user_id = s.id
      LEFT JOIN users creator ON t.created_by = creator.id
      LEFT JOIN users updater ON t.updated_by = updater.id
      WHERE (:clientId IS NULL OR t.client_id = :clientId)
      AND (:status IS NULL OR t.status = :status)
      AND (:taskType IS NULL OR t.task_type = :taskType)
      AND (:priority IS NULL OR t.priority = :priority)
      AND (:assignedStaffId IS NULL OR t.assigned_staff_id = :assignedStaffId)
      AND (:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      ORDER BY t.created_at DESC
      """, countQuery = """
      SELECT COUNT(*) FROM tasks t
      WHERE (:clientId IS NULL OR t.client_id = :clientId)
      AND (:status IS NULL OR t.status = :status)
      AND (:taskType IS NULL OR t.task_type = :taskType)
      AND (:priority IS NULL OR t.priority = :priority)
      AND (:assignedStaffId IS NULL OR t.assigned_staff_id = :assignedStaffId)
      AND (:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      """, nativeQuery = true)
  Page<Object[]> findTasksWithDetailsByFilters(
      @Param("status") String status,
      @Param("taskType") String taskType,
      @Param("priority") String priority,
      @Param("assignedStaffId") Long assignedStaffId,
      @Param("clientId") Long clientId,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  @Query(value = """
      SELECT
        t.id,
        t.title,
        t.description,
        t.due_date,
        t.status,
        t.priority,
        t.task_type,
        s.id as staff_id,
        CONCAT(u.first_name, ' ', u.last_name) as staff_name,
        (SELECT comment FROM task_comments WHERE task_id = t.id ORDER BY created_at DESC LIMIT 1) as latest_remark,
        CASE
          WHEN t.status != 'COMPLETED' AND t.due_date < CURDATE() THEN 'Overdue'
          WHEN t.status != 'COMPLETED' AND t.due_date <= DATE_ADD(CURDATE(), INTERVAL 3 DAY) THEN 'Due Soon'
          ELSE 'Safe'
        END as deadline_status,
        EXISTS (SELECT 1 FROM documents d WHERE d.task_id = t.id) as has_documents
      FROM tasks t
      LEFT JOIN staff s ON t.assigned_staff_id = s.id
      LEFT JOIN users u ON s.user_id = u.id
      WHERE t.client_id = :clientId
        AND t.status IN ('PENDING', 'IN_PROGRESS')
        AND t.due_date >= CURDATE()
      ORDER BY t.due_date
      """, nativeQuery = true)
  List<Object[]> findUpcomingDeadlinesByClientList(@Param("clientId") Long clientId);

  @Query(value = """
      SELECT
        COALESCE(COUNT(*), 0) as totalTasks,
        COALESCE(SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END), 0) as pendingTasks,
        COALESCE(SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) as inProgressTasks,
        COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) as completedTasks,
        COALESCE(SUM(CASE WHEN status = 'ON_HOLD' THEN 1 ELSE 0 END), 0) as onHoldTasks,
        COALESCE(SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END), 0) as cancelledTasks,
        COALESCE(SUM(CASE WHEN due_date < :today AND status != 'COMPLETED' THEN 1 ELSE 0 END), 0) as overdueTasks
      FROM tasks
      WHERE client_id = :clientId
      """, nativeQuery = true)
  List<Object[]> getTaskStatisticsByClient(@Param("clientId") Long clientId, @Param("today") LocalDate today);
}