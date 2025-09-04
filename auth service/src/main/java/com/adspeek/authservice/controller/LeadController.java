package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.LeadDTO;
import com.adspeek.authservice.dto.PublicLeadRequest;
import com.adspeek.authservice.entity.Lead;
import com.adspeek.authservice.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Slf4j
public class LeadController {

    private final LeadService leadService;

    // Public endpoint for lead capture (no authentication required)
    @PostMapping("/public")
    public ResponseEntity<?> createPublicLead(@RequestBody PublicLeadRequest request, HttpServletRequest httpRequest) {
        try {
            // Capture IP address and user agent
            request.setIpAddress(getClientIpAddress(httpRequest));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));

            LeadDTO lead = leadService.createPublicLead(request);
            return ResponseEntity.ok(lead);
        } catch (Exception e) {
            log.error("Error creating public lead: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating lead: " + e.getMessage());
        }
    }

    // Admin/Staff endpoints (authentication required)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> createLead(@RequestBody LeadDTO leadDTO) {
        try {
            LeadDTO lead = leadService.createLead(leadDTO);
            return ResponseEntity.ok(lead);
        } catch (Exception e) {
            log.error("Error creating lead: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating lead: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getLeadById(@PathVariable Long id) {
        try {
            LeadDTO lead = leadService.getLeadById(id);
            return ResponseEntity.ok(lead);
        } catch (Exception e) {
            log.error("Error fetching lead: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/lead-id/{leadId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getLeadByLeadId(@PathVariable String leadId) {
        try {
            LeadDTO lead = leadService.getLeadByLeadId(leadId);
            return ResponseEntity.ok(lead);
        } catch (Exception e) {
            log.error("Error fetching lead: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateLead(@PathVariable Long id, @RequestBody LeadDTO leadDTO) {
        try {
            LeadDTO lead = leadService.updateLead(id, leadDTO);
            return ResponseEntity.ok(lead);
        } catch (Exception e) {
            log.error("Error updating lead: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating lead: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/staff-update")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> updateLeadByStaff(@PathVariable Long id, @RequestBody LeadDTO leadDTO) {
        try {
            LeadDTO lead = leadService.updateLeadByStaff(id, leadDTO);
            return ResponseEntity.ok(lead);
        } catch (Exception e) {
            log.error("Error updating lead by staff: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating lead: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteLead(@PathVariable Long id) {
        try {
            leadService.deleteLead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting lead: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting lead: " + e.getMessage());
        }
    }

    // Lead listing endpoints
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getAllLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<LeadDTO> leads = leadService.getAllLeads(page, size);
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            log.error("Error fetching all leads: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getLeadsWithFilters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Lead.Status status,
            @RequestParam(required = false) Lead.Priority priority,
            @RequestParam(required = false) Long serviceCategoryId,
            @RequestParam(required = false) Long serviceSubcategoryId,
            @RequestParam(required = false) Long serviceItemId) {
        try {
            Page<LeadDTO> leads;

            // Use the new comprehensive filter method that supports service hierarchy
            leads = leadService.getLeadsWithServiceHierarchyFilters(page, size, search, status, priority,
                    serviceCategoryId, serviceSubcategoryId, serviceItemId);

            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            log.error("Error fetching leads with filters: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Staff-specific endpoints
    @GetMapping("/assigned/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getLeadsByAssignedStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Lead.Status status,
            @RequestParam(required = false) Lead.Priority priority,
            @RequestParam(required = false) Long serviceCategoryId,
            @RequestParam(required = false) Long serviceSubcategoryId,
            @RequestParam(required = false) Long serviceItemId) {
        try {
            Page<LeadDTO> leads = leadService.getLeadsByAssignedStaffWithServiceHierarchyFilters(
                    staffId, page, size, search, status, priority, serviceCategoryId, serviceSubcategoryId,
                    serviceItemId);
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            log.error("Error fetching leads by assigned staff: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Lead management endpoints
    @PostMapping("/{leadId}/assign/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> assignLeadToStaff(@PathVariable Long leadId, @PathVariable Long staffId) {
        try {
            LeadDTO lead = leadService.assignLeadToStaff(leadId, staffId);
            return ResponseEntity.ok(lead);
        } catch (Exception e) {
            log.error("Error assigning lead to staff: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error assigning lead: " + e.getMessage());
        }
    }

    @PutMapping("/{leadId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> updateLeadStatus(
            @PathVariable Long leadId,
            @RequestParam Lead.Status status,
            @RequestParam(required = false) String reason) {
        try {
            LeadDTO lead = leadService.updateLeadStatus(leadId, status, reason);
            return ResponseEntity.ok(lead);
        } catch (Exception e) {
            log.error("Error updating lead status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating lead status: " + e.getMessage());
        }
    }

    @PostMapping("/{leadId}/convert")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> convertLeadToClient(@PathVariable Long leadId) {
        try {
            LeadDTO lead = leadService.convertLeadToClient(leadId);
            return ResponseEntity.ok(lead);
        } catch (Exception e) {
            log.error("Error converting lead to client: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error converting lead: " + e.getMessage());
        }
    }

    // Follow-up endpoints
    @GetMapping("/follow-up/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getLeadsByFollowUpDate(@PathVariable String date) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            List<LeadDTO> leads = leadService.getLeadsByFollowUpDate(localDate);
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            log.error("Error fetching leads by follow-up date: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/follow-up/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getOverdueFollowUps() {
        try {
            List<LeadDTO> leads = leadService.getOverdueFollowUps();
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            log.error("Error fetching overdue follow-ups: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Statistics endpoints
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getLeadStatistics() {
        try {
            Map<String, Object> stats = leadService.getLeadStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching lead statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/statistics/staff/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getLeadStatisticsByStaff(@PathVariable Long staffId) {
        try {
            Map<String, Object> stats = leadService.getLeadStatisticsByStaff(staffId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching lead statistics by staff: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getRecentLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        try {
            Page<LeadDTO> leads = leadService.getRecentLeads(page, size);
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            log.error("Error fetching recent leads: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
