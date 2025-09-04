package com.adspeek.authservice.repository;

import com.adspeek.authservice.entity.StaffActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StaffActivityRepository extends JpaRepository<StaffActivity, Long> {

        // Find activities by staff and date range
        @Query("SELECT sa FROM StaffActivity sa WHERE sa.staff.id = :staffId AND sa.logDate BETWEEN :startDate AND :endDate ORDER BY sa.logDate DESC, sa.createdAt DESC")
        Page<StaffActivity> findByStaffIdAndDateRange(@Param("staffId") Long staffId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        // Find activities by date
        @Query("SELECT sa FROM StaffActivity sa WHERE sa.logDate = :date ORDER BY sa.createdAt DESC")
        Page<StaffActivity> findByDate(@Param("date") LocalDate date, Pageable pageable);

        // Find activities by staff
        @Query("SELECT sa FROM StaffActivity sa WHERE sa.staff.id = :staffId ORDER BY sa.logDate DESC, sa.createdAt DESC")
        Page<StaffActivity> findByStaffId(@Param("staffId") Long staffId, Pageable pageable);

        // Find login activities for a staff member on a specific date
        @Query("SELECT sa FROM StaffActivity sa WHERE sa.staff.id = :staffId AND sa.logDate = :date AND sa.activityType = 'LOGIN' ORDER BY sa.loginTime")
        List<StaffActivity> findLoginActivitiesByStaffAndDate(@Param("staffId") Long staffId,
                        @Param("date") LocalDate date);

        // Find logout activities for a staff member on a specific date
        @Query("SELECT sa FROM StaffActivity sa WHERE sa.staff.id = :staffId AND sa.logDate = :date AND sa.activityType = 'LOGOUT' ORDER BY sa.logoutTime")
        List<StaffActivity> findLogoutActivitiesByStaffAndDate(@Param("staffId") Long staffId,
                        @Param("date") LocalDate date);

        // Find task activities by status
        @Query("SELECT sa FROM StaffActivity sa WHERE sa.workStatus = :status ORDER BY sa.logDate DESC, sa.createdAt DESC")
        Page<StaffActivity> findByWorkStatus(@Param("status") StaffActivity.WorkStatus status, Pageable pageable);

        // Find activities by activity type
        @Query("SELECT sa FROM StaffActivity sa WHERE sa.activityType = :activityType ORDER BY sa.logDate DESC, sa.createdAt DESC")
        Page<StaffActivity> findByActivityType(@Param("activityType") StaffActivity.ActivityType activityType,
                        Pageable pageable);

        // Get daily summary for a staff member
        @Query(value = """
                        SELECT
                            sa.log_date,
                            COUNT(sa.id) as total_activities,
                            COUNT(CASE WHEN sa.activity_type = 'LOGIN' THEN 1 END) as login_count,
                            COUNT(CASE WHEN sa.activity_type = 'LOGOUT' THEN 1 END) as logout_count,
                            COUNT(CASE WHEN sa.work_status = 'COMPLETED' AND sa.activity_type != 'LOGIN' AND sa.activity_type != 'LOGOUT' THEN 1 END) as completed_tasks,
                            COUNT(CASE WHEN sa.work_status = 'PENDING' THEN 1 END) as pending_tasks,
                            COUNT(CASE WHEN sa.work_status = 'DELAYED' THEN 1 END) as delayed_tasks,
                            COALESCE(SUM(sa.duration_minutes), 0) as total_duration_minutes
                        FROM staff_activities sa
                        WHERE sa.staff_id = :staffId AND sa.log_date BETWEEN :startDate AND :endDate
                        GROUP BY sa.log_date
                        ORDER BY sa.log_date DESC
                        """, nativeQuery = true)
        List<Object[]> getDailySummaryByStaff(@Param("staffId") Long staffId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Get workload summary for all staff
        @Query(value = """
                        SELECT
                            s.id as staff_id,
                            CONCAT(u.first_name, ' ', u.last_name) as staff_name,
                            s.employee_id,
                            s.department,
                            COUNT(DISTINCT sa.id) as total_activities,
                            COUNT(DISTINCT CASE WHEN sa.work_status = 'COMPLETED' AND sa.activity_type != 'LOGIN' AND sa.activity_type != 'LOGOUT' THEN sa.id END) as completed_tasks,
                            COUNT(DISTINCT CASE WHEN sa.work_status = 'PENDING' THEN sa.id END) as pending_tasks,
                            COUNT(DISTINCT CASE WHEN sa.work_status = 'DELAYED' THEN sa.id END) as delayed_tasks,
                            COUNT(DISTINCT sa.client_id) as assigned_clients,
                            COALESCE(SUM(sa.duration_minutes), 0) as total_work_minutes,
                            MAX(sa.created_at) as last_activity
                        FROM staff s
                        JOIN users u ON s.user_id = u.id
                        LEFT JOIN staff_activities sa ON s.id = sa.staff_id AND sa.log_date = :date
                        GROUP BY s.id, u.first_name, u.last_name, s.employee_id, s.department
                        ORDER BY total_activities DESC
                        """, nativeQuery = true)
        List<Object[]> getWorkloadSummaryByDate(@Param("date") LocalDate date);
}