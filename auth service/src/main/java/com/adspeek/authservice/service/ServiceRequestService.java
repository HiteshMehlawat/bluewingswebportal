package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.ServiceRequestDTO;
import com.adspeek.authservice.dto.ServiceRequestDetailDTO;
import com.adspeek.authservice.dto.TaskConversionDTO;
import com.adspeek.authservice.entity.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface ServiceRequestService {

        // Basic CRUD operations
        ServiceRequestDTO createServiceRequest(ServiceRequestDTO serviceRequestDTO);

        ServiceRequestDTO updateServiceRequest(Long id, ServiceRequestDTO serviceRequestDTO);

        void deleteServiceRequest(Long id);

        Optional<ServiceRequestDetailDTO> getServiceRequestById(Long id);

        Optional<ServiceRequestDetailDTO> getServiceRequestByRequestId(String requestId);

        // Paginated listing with filters
        Page<ServiceRequestDTO> getAllServiceRequests(Pageable pageable, String search, String statusFilter,
                        String priorityFilter);

        // Client-specific operations
        Page<ServiceRequestDTO> getClientServiceRequests(Long clientId, Pageable pageable, String search,
                        String statusFilter);

        Page<ServiceRequestDTO> getMyServiceRequests(Pageable pageable, String search, String statusFilter);

        // Staff-specific operations
        Page<ServiceRequestDTO> getAssignedServiceRequests(Long staffId, Pageable pageable, String search,
                        String statusFilter);

        Page<ServiceRequestDTO> getMyAssignedServiceRequests(Pageable pageable, String search, String statusFilter);

        // Admin operations
        ServiceRequestDTO assignServiceRequestToStaff(Long requestId, Long staffId, String adminNotes);

        ServiceRequestDTO rejectServiceRequest(Long requestId, String rejectionReason);

        ServiceRequestDTO updateServiceRequestStatus(Long requestId, ServiceRequest.Status status);

        // Staff operations
        ServiceRequestDTO acceptServiceRequest(Long requestId, String staffNotes);

        ServiceRequestDTO convertServiceRequestToTask(Long requestId, TaskConversionDTO taskData);

        // Statistics
        Map<String, Object> getServiceRequestStatistics();

        Map<String, Object> getClientServiceRequestStatistics(Long clientId);

        Map<String, Object> getStaffServiceRequestStatistics(Long staffId);

        // Utility methods
        boolean canEditServiceRequest(Long requestId);

        boolean canDeleteServiceRequest(Long requestId);

        boolean canAssignServiceRequest(Long requestId);

        boolean canRejectServiceRequest(Long requestId);

        boolean canAcceptServiceRequest(Long requestId);

        boolean canConvertToTask(Long requestId);
}
