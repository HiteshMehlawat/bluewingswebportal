package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.StaffDTO;
import com.adspeek.authservice.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;
import com.adspeek.authservice.dto.StaffPerformanceSummaryDTO;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staffService;

    @GetMapping
    public List<StaffDTO> getAllStaff() {
        return staffService.getAllStaff();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffDTO> getStaffById(@PathVariable Long id) {
        Optional<StaffDTO> staff = staffService.getStaffById(id);
        return staff.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/paged")
    public Page<StaffDTO> getStaffPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        return staffService.getStaffPageWithFilters(PageRequest.of(page, size), search, department, status);
    }

    @GetMapping("/departments")
    public List<String> getAllDepartments() {
        return staffService.getAllDepartments();
    }

    @GetMapping("/{id}/performance")
    public ResponseEntity<StaffPerformanceSummaryDTO> getStaffPerformanceById(@PathVariable Long id) {
        Optional<StaffPerformanceSummaryDTO> performance = staffService.getStaffPerformanceById(id);
        return performance.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<StaffDTO> getCurrentStaff() {
        StaffDTO currentStaff = staffService.getCurrentStaff();
        return currentStaff != null ? ResponseEntity.ok(currentStaff) : ResponseEntity.notFound().build();
    }

    @GetMapping("/my-staff-id")
    public ResponseEntity<StaffIdResponse> getCurrentStaffId() {
        StaffDTO currentStaff = staffService.getCurrentStaff();
        if (currentStaff != null) {
            return ResponseEntity.ok(new StaffIdResponse(currentStaff.getId()));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/me")
    public ResponseEntity<StaffDTO> updateMyProfile(@RequestBody StaffDTO staffDTO) {
        StaffDTO updated = staffService.updateMyProfile(staffDTO);
        if (updated == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/performance")
    public Page<StaffPerformanceSummaryDTO> getStaffPerformanceSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return staffService.getStaffPerformanceSummary(PageRequest.of(page, size));
    }

    @PostMapping
    public ResponseEntity<StaffDTO> createStaff(@RequestBody StaffDTO staffDTO) {
        StaffDTO created = staffService.createStaff(staffDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffDTO> updateStaff(@PathVariable Long id, @RequestBody StaffDTO staffDTO) {
        StaffDTO updated = staffService.updateStaff(id, staffDTO);
        if (updated == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }

    // Inner class for staff ID response
    public static class StaffIdResponse {
        private Long id;

        public StaffIdResponse(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}