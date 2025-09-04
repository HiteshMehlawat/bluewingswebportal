package com.adspeek.authservice.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AdminDashboardRepository {
    @PersistenceContext
    private EntityManager em;

    public Map<String, Object> getSummary() {
        Object[] row = (Object[]) em.createNativeQuery("SELECT * FROM admin_dashboard_summary").getSingleResult();
        Map<String, Object> map = new HashMap<>();
        map.put("total_clients", row[0]);
        map.put("active_cases", row[1]);
        map.put("pending_tasks", row[2]);
        map.put("completed_this_month", row[3]);
        map.put("total_staff", row[4]);
        return map;
    }

    public List<Object[]> getStaffPerformance(Long staffId, String startDate, String endDate) {
        return em.createNativeQuery("CALL GetStaffPerformance(:staffId, :startDate, :endDate)")
                .setParameter("staffId", staffId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<Object[]> getLatestUploads() {
        return em.createNativeQuery(
                "SELECT c.company_name, d.file_name, d.upload_date FROM documents d JOIN clients c ON d.client_id = c.id ORDER BY d.upload_date DESC LIMIT 3")
                .getResultList();
    }

    public List<Object[]> getUpcomingDeadlines() {
        return em.createNativeQuery(
                "SELECT t.title, t.due_date FROM tasks t WHERE t.due_date >= CURDATE() AND t.status != 'COMPLETED' ORDER BY t.due_date ASC LIMIT 3")
                .getResultList();
    }

    public List<Object[]> getAllUpcomingDeadlines() {
        return em
                .createNativeQuery(
                        """
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
                                WHERE t.status IN ('PENDING', 'IN_PROGRESS')
                                AND t.due_date >= CURDATE()
                                ORDER BY t.due_date ASC
                                """)
                .getResultList();
    }

    public Long getCompletedFilingsCount() {
        // Count all completed tasks (not just filing-related tasks)
        Object result = em.createNativeQuery(
                "SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED'")
                .getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public List<Object[]> getWeeklyChartData() {
        return em.createNativeQuery(
                "SELECT CONCAT(YEAR(completed_date), '-W', LPAD(WEEK(completed_date, 1), 2, '0')) as week_label, COUNT(*) as count "
                        +
                        "FROM tasks WHERE status = 'COMPLETED' AND completed_date >= DATE_SUB(CURDATE(), INTERVAL 6 WEEK) "
                        +
                        "GROUP BY week_label ORDER BY week_label")
                .getResultList();
    }

    public List<Object[]> getMonthlyChartData() {
        return em.createNativeQuery(
                "SELECT DATE_FORMAT(completed_date, '%b %Y') as month_label, COUNT(*) as count " +
                        "FROM tasks WHERE status = 'COMPLETED' AND completed_date >= DATE_SUB(CURDATE(), INTERVAL 11 MONTH) "
                        +
                        "GROUP BY month_label ORDER BY MIN(completed_date)")
                .getResultList();
    }
}