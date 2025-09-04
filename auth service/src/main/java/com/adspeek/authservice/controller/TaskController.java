package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.TaskDTO;
import com.adspeek.authservice.dto.TaskDetailDTO;
import com.adspeek.authservice.dto.TaskStatisticsDTO;
import com.adspeek.authservice.dto.StaffWorkloadDTO;
import com.adspeek.authservice.entity.Task;
import com.adspeek.authservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;
import com.adspeek.authservice.dto.UpcomingDeadlineDTO;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public List<TaskDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        Optional<TaskDTO> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<TaskDetailDTO> getTaskDetailById(@PathVariable Long id) {
        Optional<TaskDetailDTO> task = taskService.getTaskDetailById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        TaskDTO created = taskService.createTask(taskDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        TaskDTO updated = taskService.updateTask(id, taskDTO);
        if (updated == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // Filtered task list
    @GetMapping("/filter")
    public Page<TaskDTO> getTasksWithFilters(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assignedStaffId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long serviceCategoryId,
            @RequestParam(required = false) Long serviceSubcategoryId,
            @RequestParam(required = false) Long serviceItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Convert string parameters to enums
        Task.Status statusEnum = null;
        Task.Priority priorityEnum = null;

        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Task.Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Invalid status value - will be ignored
            }
        }

        if (priority != null && !priority.isEmpty()) {
            try {
                priorityEnum = Task.Priority.valueOf(priority);
            } catch (IllegalArgumentException e) {
                // Invalid priority value - will be ignored
            }
        }

        return taskService.getTasksWithFilters(statusEnum, priorityEnum, assignedStaffId, clientId,
                searchTerm, serviceCategoryId, serviceSubcategoryId, serviceItemId,
                PageRequest.of(page, size));
    }

    // Search tasks
    @GetMapping("/search")
    public Page<TaskDTO> searchTasks(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return taskService.searchTasks(searchTerm, PageRequest.of(page, size));
    }

    // Get overdue tasks
    @GetMapping("/overdue")
    public List<TaskDTO> getOverdueTasks() {
        return taskService.getOverdueTasks();
    }

    // Get tasks due soon
    @GetMapping("/due-soon")
    public List<TaskDTO> getTasksDueSoon() {
        return taskService.getTasksDueSoon();
    }

    // Get tasks by staff
    @GetMapping("/staff/{staffId}")
    public Page<TaskDTO> getTasksByStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return taskService.getTasksByStaff(staffId, PageRequest.of(page, size));
    }

    // Get tasks by staff and client
    @GetMapping("/staff/{staffId}/client/{clientId}")
    public Page<TaskDTO> getTasksByStaffAndClient(
            @PathVariable Long staffId,
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return taskService.getTasksByStaffAndClient(staffId, clientId, PageRequest.of(page, size));
    }

    // Get task statistics
    @GetMapping("/statistics")
    public TaskStatisticsDTO getTaskStatistics() {
        return taskService.getTaskStatistics();
    }

    // Get staff workload summary
    @GetMapping("/workload-summary")
    public List<StaffWorkloadDTO> getStaffWorkloadSummary() {
        return taskService.getStaffWorkloadSummary();
    }

    // Get available staff for assignment
    @GetMapping("/available-staff")
    public List<StaffWorkloadDTO> getAvailableStaffForAssignment() {
        return taskService.getAvailableStaffForAssignment();
    }

    // Reassign task
    @PutMapping("/{taskId}/reassign")
    public ResponseEntity<TaskDTO> reassignTask(
            @PathVariable Long taskId,
            @RequestParam Long newStaffId) {
        TaskDTO updated = taskService.reassignTask(taskId, newStaffId);
        if (updated == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // Update task status
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Task.Status newStatus) {
        TaskDTO updated = taskService.updateTaskStatus(taskId, newStatus);
        if (updated == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/upcoming-deadlines")
    public Page<UpcomingDeadlineDTO> getUpcomingDeadlinesByClient(
            @RequestParam Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return taskService.getUpcomingDeadlinesByClient(clientId, PageRequest.of(page, size));
    }

}