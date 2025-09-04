package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.ServiceRequestDTO;
import com.adspeek.authservice.dto.ServiceRequestDetailDTO;
import com.adspeek.authservice.dto.TaskConversionDTO;
import com.adspeek.authservice.entity.ServiceRequest;
import com.adspeek.authservice.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    // Create service request (Admin, Staff, Client)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<?> createServiceRequest(@RequestBody ServiceRequestDTO serviceRequestDTO) {
        try {
            ServiceRequestDTO createdRequest = serviceRequestService.createServiceRequest(serviceRequestDTO);
            return ResponseEntity.ok(createdRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating service request: " + e.getMessage());
        }
    }

    // Update service request
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<?> updateServiceRequest(@PathVariable Long id,
            @RequestBody ServiceRequestDTO serviceRequestDTO) {
        try {
            ServiceRequestDTO updatedRequest = serviceRequestService.updateServiceRequest(id, serviceRequestDTO);
            return ResponseEntity.ok(updatedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating service request: " + e.getMessage());
        }
    }

    // Delete service request (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteServiceRequest(@PathVariable Long id) {
        try {
            serviceRequestService.deleteServiceRequest(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting service request: " + e.getMessage());
        }
    }

    // Get service request by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<?> getServiceRequestById(@PathVariable Long id) {
        try {
            Optional<ServiceRequestDetailDTO> serviceRequest = serviceRequestService.getServiceRequestById(id);
            if (serviceRequest.isPresent()) {
                return ResponseEntity.ok(serviceRequest.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching service request: " + e.getMessage());
        }
    }

    // Get service request by request ID
    @GetMapping("/by-request-id/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<?> getServiceRequestByRequestId(@PathVariable String requestId) {
        try {
            Optional<ServiceRequestDetailDTO> serviceRequest = serviceRequestService
                    .getServiceRequestByRequestId(requestId);
            if (serviceRequest.isPresent()) {
                return ResponseEntity.ok(serviceRequest.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching service request: " + e.getMessage());
        }
    }

    // Get all service requests (Admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllServiceRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) String priorityFilter) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ServiceRequestDTO> serviceRequests = serviceRequestService.getAllServiceRequests(
                    pageable, search, statusFilter, priorityFilter);
            return ResponseEntity.ok(serviceRequests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching service requests: " + e.getMessage());
        }
    }

    // Get client service requests (Admin only)
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getClientServiceRequests(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ServiceRequestDTO> serviceRequests = serviceRequestService.getClientServiceRequests(
                    clientId, pageable, search, statusFilter);
            return ResponseEntity.ok(serviceRequests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching client service requests: " + e.getMessage());
        }
    }

    // Get my service requests (Client only)
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> getMyServiceRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ServiceRequestDTO> serviceRequests = serviceRequestService.getMyServiceRequests(
                    pageable, search, statusFilter);
            return ResponseEntity.ok(serviceRequests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching my service requests: " + e.getMessage());
        }
    }

    // Get assigned service requests (Admin only)
    @GetMapping("/assigned/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAssignedServiceRequests(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ServiceRequestDTO> serviceRequests = serviceRequestService.getAssignedServiceRequests(
                    staffId, pageable, search, statusFilter);
            return ResponseEntity.ok(serviceRequests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching assigned service requests: " + e.getMessage());
        }
    }

    // Get my assigned service requests (Staff only)
    @GetMapping("/my-assigned")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getMyAssignedServiceRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ServiceRequestDTO> serviceRequests = serviceRequestService.getMyAssignedServiceRequests(
                    pageable, search, statusFilter);
            return ResponseEntity.ok(serviceRequests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching my assigned service requests: " + e.getMessage());
        }
    }

    // Assign service request to staff (Admin only)
    @PostMapping("/{requestId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignServiceRequestToStaff(
            @PathVariable Long requestId,
            @RequestParam Long staffId,
            @RequestParam(required = false) String adminNotes) {
        try {
            ServiceRequestDTO assignedRequest = serviceRequestService.assignServiceRequestToStaff(requestId, staffId,
                    adminNotes);
            return ResponseEntity.ok(assignedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error assigning service request: " + e.getMessage());
        }
    }

    // Reject service request (Admin or Staff)
    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> rejectServiceRequest(
            @PathVariable Long requestId,
            @RequestParam String rejectionReason) {
        try {
            ServiceRequestDTO rejectedRequest = serviceRequestService.rejectServiceRequest(requestId, rejectionReason);
            return ResponseEntity.ok(rejectedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error rejecting service request: " + e.getMessage());
        }
    }

    // Update service request status
    @PutMapping("/{requestId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<?> updateServiceRequestStatus(
            @PathVariable Long requestId,
            @RequestParam String status) {
        try {
            ServiceRequest.Status statusEnum = ServiceRequest.Status.valueOf(status.toUpperCase());
            ServiceRequestDTO updatedRequest = serviceRequestService.updateServiceRequestStatus(requestId, statusEnum);
            return ResponseEntity.ok(updatedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating service request status: " + e.getMessage());
        }
    }

    // Accept service request (Staff only)
    @PostMapping("/{requestId}/accept")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> acceptServiceRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String staffNotes) {
        try {
            ServiceRequestDTO acceptedRequest = serviceRequestService.acceptServiceRequest(requestId, staffNotes);
            return ResponseEntity.ok(acceptedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error accepting service request: " + e.getMessage());
        }
    }

    // Convert service request to task (Staff and Admin only)
    @PostMapping("/{requestId}/convert-to-task")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> convertServiceRequestToTask(
            @PathVariable Long requestId,
            @RequestBody TaskConversionDTO taskData) {
        try {
            if (taskData.getTitle() == null || taskData.getDescription() == null) {
                return ResponseEntity.badRequest().body("Task title and description are required");
            }

            ServiceRequestDTO convertedRequest = serviceRequestService.convertServiceRequestToTask(requestId, taskData);
            return ResponseEntity.ok(convertedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error converting service request to task: " + e.getMessage());
        }
    }

    // Get service request statistics (Admin only)
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getServiceRequestStatistics() {
        try {
            Map<String, Object> statistics = serviceRequestService.getServiceRequestStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching service request statistics: " + e.getMessage());
        }
    }

    // Get client service request statistics
    @GetMapping("/statistics/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<?> getClientServiceRequestStatistics(@PathVariable Long clientId) {
        try {
            Map<String, Object> statistics = serviceRequestService.getClientServiceRequestStatistics(clientId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching client service request statistics: " + e.getMessage());
        }
    }

    // Get staff service request statistics
    @GetMapping("/statistics/staff/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getStaffServiceRequestStatistics(@PathVariable Long staffId) {
        try {
            Map<String, Object> statistics = serviceRequestService.getStaffServiceRequestStatistics(staffId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching staff service request statistics: " + e.getMessage());
        }
    }

    // Permission check endpoints
    @GetMapping("/{requestId}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CLIENT')")
    public ResponseEntity<?> getServiceRequestPermissions(@PathVariable Long requestId) {
        try {
            Map<String, Boolean> permissions = Map.of(
                    "canEdit", serviceRequestService.canEditServiceRequest(requestId),
                    "canDelete", serviceRequestService.canDeleteServiceRequest(requestId),
                    "canAssign", serviceRequestService.canAssignServiceRequest(requestId),
                    "canReject", serviceRequestService.canRejectServiceRequest(requestId),
                    "canAccept", serviceRequestService.canAcceptServiceRequest(requestId),
                    "canConvertToTask", serviceRequestService.canConvertToTask(requestId));
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching service request permissions: " + e.getMessage());
        }
    }
}
