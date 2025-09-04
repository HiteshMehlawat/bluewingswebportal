package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.DeadlineDTO;
import com.adspeek.authservice.dto.DeadlineStatisticsDTO;
import com.adspeek.authservice.dto.PageResponse;
import com.adspeek.authservice.entity.Task;
import com.adspeek.authservice.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<PageResponse<DeadlineDTO>> getDeadlines(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Boolean isOverdue,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DeadlineDTO> deadlines = deadlineService.getDeadlines(clientId, status, priority, taskType, isOverdue,
                searchTerm, pageable);
        return ResponseEntity.ok(PageResponse.fromPage(deadlines));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<PageResponse<DeadlineDTO>> getClientDeadlines(
            @PathVariable Long clientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Boolean isOverdue,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DeadlineDTO> deadlines = deadlineService.getClientDeadlines(clientId, status, priority, taskType,
                isOverdue, searchTerm, pageable);
        return ResponseEntity.ok(PageResponse.fromPage(deadlines));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<DeadlineDTO>> getOverdueDeadlines() {
        List<DeadlineDTO> deadlines = deadlineService.getOverdueDeadlines();
        return ResponseEntity.ok(deadlines);
    }

    @GetMapping("/due-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<DeadlineDTO>> getDueSoonDeadlines() {
        List<DeadlineDTO> deadlines = deadlineService.getDueSoonDeadlines();
        return ResponseEntity.ok(deadlines);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<DeadlineStatisticsDTO> getDeadlineStatistics() {
        DeadlineStatisticsDTO statistics = deadlineService.getDeadlineStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<DeadlineDTO> getDeadlineById(@PathVariable Long id) {
        DeadlineDTO deadline = deadlineService.getDeadlineById(id);
        return ResponseEntity.ok(deadline);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<DeadlineDTO> updateDeadlineStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        DeadlineDTO deadline = deadlineService.updateDeadlineStatus(id, status);
        return ResponseEntity.ok(deadline);
    }

    @PutMapping("/{id}/extend")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<DeadlineDTO> extendDeadline(
            @PathVariable Long id,
            @RequestParam String newDueDate) {
        DeadlineDTO deadline = deadlineService.extendDeadline(id, newDueDate);
        return ResponseEntity.ok(deadline);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<DeadlineDTO>> getDeadlinesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<DeadlineDTO> deadlines = deadlineService.getDeadlinesByDateRange(startDate, endDate);
        return ResponseEntity.ok(deadlines);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<DeadlineDTO>> getUpcomingDeadlines(
            @RequestParam(defaultValue = "10") int limit) {
        List<DeadlineDTO> deadlines = deadlineService.getUpcomingDeadlines(limit);
        return ResponseEntity.ok(deadlines);
    }

    @PostMapping("/{id}/remind")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> sendDeadlineReminder(@PathVariable Long id) {
        deadlineService.sendDeadlineReminder(id);
        return ResponseEntity.ok().build();
    }
}
