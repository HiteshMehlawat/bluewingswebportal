package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.StaffActivityDTO;
import com.adspeek.authservice.service.StaffActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.adspeek.authservice.entity.Staff;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff-activities")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class StaffActivityController {

    private final StaffActivityService staffActivityService;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    // CRUD operations
    @PostMapping
    public ResponseEntity<StaffActivityDTO> createActivity(@RequestBody StaffActivityDTO activityDTO) {
        StaffActivityDTO created = staffActivityService.createActivity(activityDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffActivityDTO> updateActivity(@PathVariable Long id,
            @RequestBody StaffActivityDTO activityDTO) {
        StaffActivityDTO updated = staffActivityService.updateActivity(id, activityDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        staffActivityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffActivityDTO> getActivityById(@PathVariable Long id) {
        StaffActivityDTO activity = staffActivityService.getActivityById(id);
        if (activity != null) {
            return ResponseEntity.ok(activity);
        }
        return ResponseEntity.notFound().build();
    }

    // Query operations
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<Page<StaffActivityDTO>> getActivitiesByStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<StaffActivityDTO> activities = staffActivityService.getActivitiesByStaff(staffId,
                PageRequest.of(page, size));
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<Page<StaffActivityDTO>> getActivitiesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<StaffActivityDTO> activities = staffActivityService.getActivitiesByDate(date, PageRequest.of(page, size));
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/staff/{staffId}/date-range")
    public ResponseEntity<Page<StaffActivityDTO>> getActivitiesByStaffAndDateRange(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<StaffActivityDTO> activities = staffActivityService.getActivitiesByStaffAndDateRange(
                staffId, startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(activities);
    }

    // Login/Logout tracking
    @PostMapping("/staff/{staffId}/login")
    public ResponseEntity<StaffActivityDTO> logLogin(@PathVariable Long staffId) {
        StaffActivityDTO loginActivity = staffActivityService.logLogin(staffId);
        return ResponseEntity.ok(loginActivity);
    }

    @PostMapping("/staff/{staffId}/logout")
    public ResponseEntity<StaffActivityDTO> logLogout(@PathVariable Long staffId) {
        StaffActivityDTO logoutActivity = staffActivityService.logLogout(staffId);
        return ResponseEntity.ok(logoutActivity);
    }

    // Task status tracking
    @PostMapping("/staff/{staffId}/start-task")
    public ResponseEntity<StaffActivityDTO> startTask(
            @PathVariable Long staffId,
            @RequestParam String taskDescription,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long clientId) {
        StaffActivityDTO taskActivity = staffActivityService.startTask(staffId, taskDescription, taskId, clientId);
        return ResponseEntity.ok(taskActivity);
    }

    @PostMapping("/staff/{staffId}/complete-task")
    public ResponseEntity<StaffActivityDTO> completeTask(
            @PathVariable Long staffId,
            @RequestParam String taskDescription,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long clientId) {
        StaffActivityDTO taskActivity = staffActivityService.completeTask(staffId, taskDescription, taskId, clientId);
        return ResponseEntity.ok(taskActivity);
    }

    @PostMapping("/staff/{staffId}/delay-task")
    public ResponseEntity<StaffActivityDTO> delayTask(
            @PathVariable Long staffId,
            @RequestParam String taskDescription,
            @RequestParam String reason,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long clientId) {
        StaffActivityDTO taskActivity = staffActivityService.delayTask(staffId, taskDescription, taskId, clientId,
                reason);
        return ResponseEntity.ok(taskActivity);
    }

    // Reports and summaries
    @GetMapping("/staff/{staffId}/daily-summary")
    public ResponseEntity<Map<String, Object>> getDailySummary(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> summary = staffActivityService.getDailySummary(staffId, date);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/staff/{staffId}/weekly-summary")
    public ResponseEntity<List<Map<String, Object>>> getWeeklySummary(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Map<String, Object>> summary = staffActivityService.getWeeklySummary(staffId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/workload-summary")
    public ResponseEntity<Map<String, Object>> getWorkloadSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> summary = staffActivityService.getWorkloadSummary(date);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/staff/{staffId}/performance")
    public ResponseEntity<Map<String, Object>> getStaffPerformance(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> performance = staffActivityService.getStaffPerformance(staffId, startDate, endDate);
        return ResponseEntity.ok(performance);
    }

    // New endpoints for staff to view their own data
    @GetMapping("/my-attendance")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Page<StaffActivityDTO>> getMyAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Get current staff ID from security context
        Long currentStaffId = getCurrentStaffId();
        Page<StaffActivityDTO> activities = staffActivityService.getActivitiesByStaff(currentStaffId,
                PageRequest.of(page, size));
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/my-activities-today")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Page<StaffActivityDTO>> getMyActivitiesToday(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Get current staff ID from security context
        Long currentStaffId = getCurrentStaffId();
        LocalDate today = LocalDate.now();
        Page<StaffActivityDTO> activities = staffActivityService.getActivitiesByStaffAndDateRange(
                currentStaffId, today, today, PageRequest.of(page, size));
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/my-attendance/date-range")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Page<StaffActivityDTO>> getMyAttendanceByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentStaffId = getCurrentStaffId();
        Page<StaffActivityDTO> activities = staffActivityService.getActivitiesByStaffAndDateRange(
                currentStaffId, startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/my-performance")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Map<String, Object>> getMyPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long currentStaffId = getCurrentStaffId();
        Map<String, Object> performance = staffActivityService.getStaffPerformance(currentStaffId, startDate, endDate);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/my-daily-summary")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Map<String, Object>> getMyDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long currentStaffId = getCurrentStaffId();
        Map<String, Object> summary = staffActivityService.getDailySummary(currentStaffId, date);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/my-weekly-summary")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<Map<String, Object>>> getMyWeeklySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long currentStaffId = getCurrentStaffId();
        List<Map<String, Object>> summary = staffActivityService.getWeeklySummary(currentStaffId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    // Helper method to get current staff ID
    private Long getCurrentStaffId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Staff staff = staffRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Staff not found"));
            return staff.getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    // Test endpoint to create sample activity data
    @PostMapping("/test/create-sample-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> createSampleActivityData() {
        try {
            Long currentStaffId = getCurrentStaffId();

            // Create sample login activity for today
            staffActivityService.logLogin(currentStaffId);

            // Create sample task activities
            staffActivityService.startTask(currentStaffId, "Sample task started", null, null);
            staffActivityService.completeTask(currentStaffId, "Sample task completed", null, null);

            // Create sample logout activity
            staffActivityService.logLogout(currentStaffId);

            return ResponseEntity.ok("Sample activity data created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating sample data: " + e.getMessage());
        }
    }

    // Test endpoint to manually trigger login activity
    @PostMapping("/test/trigger-login")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> triggerLoginActivity() {
        try {
            Long currentStaffId = getCurrentStaffId();
            staffActivityService.logLogin(currentStaffId);
            return ResponseEntity.ok("Manual login activity created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating manual login activity: " + e.getMessage());
        }
    }

    // Test endpoint to manually trigger logout activity
    @PostMapping("/test/trigger-logout")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> triggerLogoutActivity() {
        try {
            Long currentStaffId = getCurrentStaffId();
            staffActivityService.logLogout(currentStaffId);
            return ResponseEntity.ok("Manual logout activity created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating manual logout activity: " + e.getMessage());
        }
    }
}