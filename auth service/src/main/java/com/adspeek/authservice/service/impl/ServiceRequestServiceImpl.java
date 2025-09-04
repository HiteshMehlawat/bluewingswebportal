package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.ServiceRequestDTO;
import com.adspeek.authservice.dto.ServiceRequestDetailDTO;
import com.adspeek.authservice.dto.TaskConversionDTO;
import com.adspeek.authservice.entity.*;
import com.adspeek.authservice.repository.*;
import com.adspeek.authservice.service.NotificationService;
import com.adspeek.authservice.service.ServiceRequestService;
import com.adspeek.authservice.service.IdGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ClientRepository clientRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final IdGenerationService idGenerationService;

    // Helper methods
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            return user;
        }

        return null;
    }

    private Staff getCurrentStaff() {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != User.Role.STAFF) {
            return null;
        }
        return staffRepository.findByUserId(currentUser.getId()).orElse(null);
    }

    private Client getClientForUser(User user) {

        if (user.getRole() != User.Role.CLIENT) {

            return null;
        }

        Client client = clientRepository.findByUserId(user.getId()).orElse(null);

        return client;
    }

    private boolean hasAccessToServiceRequest(ServiceRequest serviceRequest) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Admin can access all service requests
        if (currentUser.getRole() == User.Role.ADMIN) {
            return true;
        }

        // Client can access their own service requests
        if (currentUser.getRole() == User.Role.CLIENT) {
            Client client = getClientForUser(currentUser);
            return client != null && client.getId().equals(serviceRequest.getClient().getId());
        }

        // Staff can access assigned service requests
        if (currentUser.getRole() == User.Role.STAFF) {
            Staff staff = getCurrentStaff();
            return staff != null && staff.getId().equals(serviceRequest.getAssignedStaff().getId());
        }

        return false;
    }

    private Task.Priority convertPriority(ServiceRequest.Priority priority) {
        switch (priority) {
            case LOW:
                return Task.Priority.LOW;
            case MEDIUM:
                return Task.Priority.MEDIUM;
            case HIGH:
                return Task.Priority.HIGH;
            case URGENT:
                return Task.Priority.HIGH; // Map URGENT to HIGH
            default:
                return Task.Priority.MEDIUM;
        }
    }

    // Implementation methods will be added in the next part
    @Override
    public ServiceRequestDTO createServiceRequest(ServiceRequestDTO serviceRequestDTO) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }

        // Handle different user roles for service request creation
        Client client = null;
        if (currentUser.getRole() == User.Role.CLIENT) {
            // Client creating their own service request
            client = getClientForUser(currentUser);
            if (client == null) {
                throw new RuntimeException("Client not found for current user");
            }
        } else if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.STAFF) {
            // Admin/Staff creating service request for a client
            if (serviceRequestDTO.getClientId() == null) {
                throw new RuntimeException("Client ID is required when creating service request as admin/staff");
            }
            client = clientRepository.findById(serviceRequestDTO.getClientId())
                    .orElseThrow(
                            () -> new RuntimeException("Client not found with ID: " + serviceRequestDTO.getClientId()));
        } else {
            throw new RuntimeException("Invalid user role for creating service request");
        }

        // Generate sequential service request ID
        String requestId = idGenerationService.generateNextServiceRequestId();

        // Get service item if provided
        ServiceItem serviceItem = null;
        if (serviceRequestDTO.getServiceItemId() != null) {
            serviceItem = serviceItemRepository.findById(serviceRequestDTO.getServiceItemId())
                    .orElse(null);
        }

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .requestId(requestId)
                .serviceCategoryName(serviceRequestDTO.getServiceCategoryName())
                .serviceSubcategoryName(serviceRequestDTO.getServiceSubcategoryName())
                .serviceItem(serviceItem)
                .serviceItemName(serviceRequestDTO.getServiceItemName())
                .description(serviceRequestDTO.getDescription())
                .notes(serviceRequestDTO.getNotes())
                .preferredDeadline(serviceRequestDTO.getPreferredDeadline())
                .priority(serviceRequestDTO.getPriority() != null ? serviceRequestDTO.getPriority()
                        : ServiceRequest.Priority.MEDIUM)
                .status(serviceRequestDTO.getStatus() != null ? serviceRequestDTO.getStatus()
                        : ServiceRequest.Status.PENDING)
                .adminNotes(serviceRequestDTO.getAdminNotes())
                .estimatedPrice(serviceRequestDTO.getEstimatedPrice())
                .finalPrice(serviceRequestDTO.getFinalPrice())
                .staffNotes(serviceRequestDTO.getStaffNotes())
                .rejectionReason(serviceRequestDTO.getRejectionReason())
                .client(client)
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Handle staff assignment
        Staff assignedStaff = null;
        if (serviceRequestDTO.getAssignedStaffId() != null) {
            // Admin explicitly assigned staff
            assignedStaff = staffRepository.findById(serviceRequestDTO.getAssignedStaffId())
                    .orElseThrow(() -> new RuntimeException(
                            "Staff not found with ID: " + serviceRequestDTO.getAssignedStaffId()));
        } else if (currentUser.getRole() == User.Role.STAFF) {
            // Staff user creating service request - auto-assign to themselves
            assignedStaff = staffRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Staff not found for current user"));
        }

        if (assignedStaff != null) {
            serviceRequest.setAssignedStaff(assignedStaff);
            serviceRequest.setAssignedDate(LocalDateTime.now());
            serviceRequest.setAssignedBy(currentUser);

            // If status is not explicitly set and staff is assigned, set status to ASSIGNED
            if (serviceRequestDTO.getStatus() == null) {
                serviceRequest.setStatus(ServiceRequest.Status.ASSIGNED);
            }
        }

        ServiceRequest savedServiceRequest = serviceRequestRepository.save(serviceRequest);

        return convertToDTO(savedServiceRequest);
    }

    @Override
    public ServiceRequestDTO updateServiceRequest(Long id, ServiceRequestDTO serviceRequestDTO) {
        ServiceRequest existingRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        // Check access permissions
        if (!hasAccessToServiceRequest(existingRequest)) {
            throw new RuntimeException("Access denied to this service request");
        }

        User currentUser = getCurrentUser();

        // Update allowed fields based on user role
        if (currentUser.getRole() == User.Role.CLIENT) {
            // Clients can only update certain fields
            existingRequest.setDescription(serviceRequestDTO.getDescription());
            existingRequest.setNotes(serviceRequestDTO.getNotes());
            existingRequest.setPreferredDeadline(serviceRequestDTO.getPreferredDeadline());
            existingRequest.setPriority(serviceRequestDTO.getPriority());
        } else if (currentUser.getRole() == User.Role.ADMIN) {
            // Admins can update most fields including status and staff assignment
            // Allow changing client if provided
            if (serviceRequestDTO.getClientId() != null) {
                Client newClient = clientRepository.findById(serviceRequestDTO.getClientId())
                        .orElseThrow(() -> new RuntimeException(
                                "Client not found with ID: " + serviceRequestDTO.getClientId()));
                existingRequest.setClient(newClient);
            }
            existingRequest.setServiceCategoryName(serviceRequestDTO.getServiceCategoryName());
            existingRequest.setServiceSubcategoryName(serviceRequestDTO.getServiceSubcategoryName());
            existingRequest.setServiceItemName(serviceRequestDTO.getServiceItemName());

            // Update the service item ID - this was missing and causing the issue
            if (serviceRequestDTO.getServiceItemId() != null) {
                existingRequest.setServiceItem(serviceItemRepository.findById(serviceRequestDTO.getServiceItemId())
                        .orElseThrow(() -> new RuntimeException(
                                "Service item not found with ID: " + serviceRequestDTO.getServiceItemId())));
            }

            existingRequest.setDescription(serviceRequestDTO.getDescription());
            existingRequest.setNotes(serviceRequestDTO.getNotes());
            existingRequest.setPreferredDeadline(serviceRequestDTO.getPreferredDeadline());
            existingRequest.setPriority(serviceRequestDTO.getPriority());
            existingRequest.setAdminNotes(serviceRequestDTO.getAdminNotes());
            existingRequest.setEstimatedPrice(serviceRequestDTO.getEstimatedPrice());
            existingRequest.setFinalPrice(serviceRequestDTO.getFinalPrice());

            // Handle status updates
            if (serviceRequestDTO.getStatus() != null) {
                existingRequest.setStatus(serviceRequestDTO.getStatus());

                // If status is being set to CANCELLED, clear staff assignment
                if (serviceRequestDTO.getStatus() == ServiceRequest.Status.CANCELLED) {
                    existingRequest.setAssignedStaff(null);
                    existingRequest.setAssignedDate(null);
                    existingRequest.setAssignedBy(null);
                }
            }

            // Handle staff assignment updates
            if (serviceRequestDTO.getAssignedStaffId() != null) {
                Staff assignedStaff = staffRepository.findById(serviceRequestDTO.getAssignedStaffId())
                        .orElseThrow(() -> new RuntimeException(
                                "Staff not found with ID: " + serviceRequestDTO.getAssignedStaffId()));
                existingRequest.setAssignedStaff(assignedStaff);
                existingRequest.setAssignedDate(LocalDateTime.now());
                existingRequest.setAssignedBy(currentUser);

                // If status is PENDING and staff is assigned, set status to ASSIGNED
                if (existingRequest.getStatus() == ServiceRequest.Status.PENDING) {
                    existingRequest.setStatus(ServiceRequest.Status.ASSIGNED);
                }
            } else if (existingRequest.getAssignedStaff() != null && serviceRequestDTO.getAssignedStaffId() == null) {
                // If staff assignment is being removed
                existingRequest.setAssignedStaff(null);
                existingRequest.setAssignedDate(null);
                existingRequest.setAssignedBy(null);

                // If status was ASSIGNED, revert to PENDING
                if (existingRequest.getStatus() == ServiceRequest.Status.ASSIGNED) {
                    existingRequest.setStatus(ServiceRequest.Status.PENDING);
                }
            }
        } else if (currentUser.getRole() == User.Role.STAFF) {

            // Allow changing client if provided
            if (serviceRequestDTO.getClientId() != null) {
                Client newClient = clientRepository.findById(serviceRequestDTO.getClientId())
                        .orElseThrow(() -> new RuntimeException(
                                "Client not found with ID: " + serviceRequestDTO.getClientId()));
                existingRequest.setClient(newClient);
            }
            // Staff can update their notes, status, pricing, and most fields on requests
            // assigned to them
            existingRequest.setStaffNotes(serviceRequestDTO.getStaffNotes());

            // Update general details
            if (serviceRequestDTO.getDescription() != null)
                existingRequest.setDescription(serviceRequestDTO.getDescription());
            if (serviceRequestDTO.getNotes() != null)
                existingRequest.setNotes(serviceRequestDTO.getNotes());
            if (serviceRequestDTO.getPreferredDeadline() != null)
                existingRequest.setPreferredDeadline(serviceRequestDTO.getPreferredDeadline());
            if (serviceRequestDTO.getPriority() != null)
                existingRequest.setPriority(serviceRequestDTO.getPriority());

            // Pricing and internal notes (allowed for staff)
            if (serviceRequestDTO.getAdminNotes() != null)
                existingRequest.setAdminNotes(serviceRequestDTO.getAdminNotes());
            if (serviceRequestDTO.getEstimatedPrice() != null)
                existingRequest.setEstimatedPrice(serviceRequestDTO.getEstimatedPrice());
            if (serviceRequestDTO.getFinalPrice() != null)
                existingRequest.setFinalPrice(serviceRequestDTO.getFinalPrice());

            // Status
            if (serviceRequestDTO.getStatus() != null) {
                existingRequest.setStatus(serviceRequestDTO.getStatus());
            }

            // Service hierarchy updates
            if (serviceRequestDTO.getServiceItemId() != null) {
                existingRequest.setServiceItem(serviceItemRepository.findById(serviceRequestDTO.getServiceItemId())
                        .orElseThrow(() -> new RuntimeException(
                                "Service item not found with ID: " + serviceRequestDTO.getServiceItemId())));
                existingRequest.setServiceCategoryName(serviceRequestDTO.getServiceCategoryName());
                existingRequest.setServiceSubcategoryName(serviceRequestDTO.getServiceSubcategoryName());
                existingRequest.setServiceItemName(serviceRequestDTO.getServiceItemName());
            }

            // Optionally allow staff to change client if necessary
            if (serviceRequestDTO.getClientId() != null) {
                Client newClient = clientRepository.findById(serviceRequestDTO.getClientId())
                        .orElseThrow(() -> new RuntimeException(
                                "Client not found with ID: " + serviceRequestDTO.getClientId()));
                existingRequest.setClient(newClient);
            }
        }

        existingRequest.setUpdatedBy(currentUser);
        existingRequest.setUpdatedAt(LocalDateTime.now());

        // Store old values for notification purposes
        ServiceRequest.Status oldStatus = existingRequest.getStatus();
        Staff oldAssignedStaff = existingRequest.getAssignedStaff();

        ServiceRequest savedRequest = serviceRequestRepository.save(existingRequest);

        // Send notifications based on what changed
        try {
            // Notify client about status changes (if status changed and not by client)
            if (currentUser.getRole() != User.Role.CLIENT && oldStatus != savedRequest.getStatus()) {
                notificationService.notifyServiceRequestStatusChanged(
                        savedRequest.getId(),
                        oldStatus.toString(),
                        savedRequest.getStatus().toString(),
                        currentUser.getId());
            }

            // Notify client about staff assignment (if staff was assigned and not by
            // client)
            if (currentUser.getRole() != User.Role.CLIENT &&
                    (oldAssignedStaff == null && savedRequest.getAssignedStaff() != null)) {
                notificationService.notifyServiceRequestAssigned(
                        savedRequest.getId(),
                        savedRequest.getClient().getUser().getId(),
                        savedRequest.getAssignedStaff().getUser().getId());
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
            log.error("Failed to send notifications for service request update: {}", e.getMessage());
        }

        return convertToDTO(savedRequest);
    }

    @Override
    public void deleteServiceRequest(Long id) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admins can delete service requests");
        }

        serviceRequestRepository.delete(serviceRequest);

    }

    @Override
    public Optional<ServiceRequestDetailDTO> getServiceRequestById(Long id) {
        return serviceRequestRepository.findById(id)
                .map(this::convertToDetailDTO);
    }

    @Override
    public Optional<ServiceRequestDetailDTO> getServiceRequestByRequestId(String requestId) {
        return serviceRequestRepository.findByRequestId(requestId)
                .map(this::convertToDetailDTO);
    }

    @Override
    public Page<ServiceRequestDTO> getAllServiceRequests(Pageable pageable, String search, String statusFilter,
            String priorityFilter) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admins can view all service requests");
        }

        Page<ServiceRequest> serviceRequests = serviceRequestRepository.findAllWithFilters(
                search, statusFilter, priorityFilter, pageable);

        return serviceRequests.map(this::convertToDTO);
    }

    @Override
    public Page<ServiceRequestDTO> getClientServiceRequests(Long clientId, Pageable pageable, String search,
            String statusFilter) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admins can view client service requests");
        }

        Page<ServiceRequest> serviceRequests;
        if (search != null && !search.trim().isEmpty()) {
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                serviceRequests = serviceRequestRepository.findBySearchTermAndStatus(search.trim(),
                        ServiceRequest.Status.valueOf(statusFilter), pageable);
            } else {
                serviceRequests = serviceRequestRepository.findBySearchTerm(search.trim(), pageable);
            }
        } else {
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                serviceRequests = serviceRequestRepository.findByClientIdAndStatus(clientId,
                        ServiceRequest.Status.valueOf(statusFilter), pageable);
            } else {
                serviceRequests = serviceRequestRepository.findByClientId(clientId, pageable);
            }
        }

        return serviceRequests.map(this::convertToDTO);
    }

    @Override
    public Page<ServiceRequestDTO> getMyServiceRequests(Pageable pageable, String search, String statusFilter) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.CLIENT) {
            throw new RuntimeException("Only clients can view their own service requests");
        }

        Client client = getClientForUser(currentUser);
        if (client == null) {
            throw new RuntimeException("Client not found for current user");
        }

        Page<ServiceRequest> serviceRequests;
        if (search != null && !search.trim().isEmpty()) {
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                serviceRequests = serviceRequestRepository.findBySearchTermAndStatus(search.trim(),
                        ServiceRequest.Status.valueOf(statusFilter), pageable);
            } else {
                serviceRequests = serviceRequestRepository.findBySearchTerm(search.trim(), pageable);
            }
        } else {
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                serviceRequests = serviceRequestRepository.findByClientIdAndStatus(client.getId(),
                        ServiceRequest.Status.valueOf(statusFilter), pageable);
            } else {
                serviceRequests = serviceRequestRepository.findByClientId(client.getId(), pageable);
            }
        }

        return serviceRequests.map(this::convertToDTO);
    }

    @Override
    public Page<ServiceRequestDTO> getAssignedServiceRequests(Long staffId, Pageable pageable, String search,
            String statusFilter) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admins can view assigned service requests");
        }

        Page<ServiceRequest> serviceRequests;
        if (search != null && !search.trim().isEmpty()) {
            // For admin viewing assigned requests, we need to filter by assigned staff
            // This would need a custom query method
            serviceRequests = serviceRequestRepository.findByAssignedStaffId(staffId, pageable);
        } else {
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                serviceRequests = serviceRequestRepository.findByAssignedStaffIdAndStatus(staffId,
                        ServiceRequest.Status.valueOf(statusFilter), pageable);
            } else {
                serviceRequests = serviceRequestRepository.findByAssignedStaffId(staffId, pageable);
            }
        }

        return serviceRequests.map(this::convertToDTO);
    }

    @Override
    public Page<ServiceRequestDTO> getMyAssignedServiceRequests(Pageable pageable, String search, String statusFilter) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.STAFF) {
            throw new RuntimeException("Only staff can view their assigned service requests");
        }

        Staff staff = getCurrentStaff();
        if (staff == null) {
            throw new RuntimeException("Staff not found for current user");
        }

        Page<ServiceRequest> serviceRequests;
        if (search != null && !search.trim().isEmpty()) {
            // For staff viewing their assigned requests, we need to filter by assigned
            // staff
            // This would need a custom query method
            serviceRequests = serviceRequestRepository.findByAssignedStaffId(staff.getId(), pageable);
        } else {
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                serviceRequests = serviceRequestRepository.findByAssignedStaffIdAndStatus(staff.getId(),
                        ServiceRequest.Status.valueOf(statusFilter), pageable);
            } else {
                serviceRequests = serviceRequestRepository.findByAssignedStaffId(staff.getId(), pageable);
            }
        }

        return serviceRequests.map(this::convertToDTO);
    }

    @Override
    public ServiceRequestDTO assignServiceRequestToStaff(Long requestId, Long staffId, String adminNotes) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admins can assign service requests");
        }

        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        serviceRequest.setAssignedStaff(staff);
        serviceRequest.setStatus(ServiceRequest.Status.ASSIGNED);
        serviceRequest.setAssignedDate(LocalDateTime.now());
        serviceRequest.setAssignedBy(currentUser);
        serviceRequest.setAdminNotes(adminNotes);
        serviceRequest.setUpdatedBy(currentUser);
        serviceRequest.setUpdatedAt(LocalDateTime.now());

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        // Send notification to staff
        // TODO: Implement notification service method
        // try {
        // notificationService.notifyServiceRequestAssigned(savedRequest.getId(),
        // staffId);
        // } catch (Exception e) {
        // log.error("Failed to send notification for service request assignment: {}",
        // e.getMessage());
        // }

        return convertToDTO(savedRequest);
    }

    @Override
    public ServiceRequestDTO rejectServiceRequest(Long requestId, String rejectionReason) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN && currentUser.getRole() != User.Role.STAFF) {
            throw new RuntimeException("Only admins or staff can reject service requests");
        }

        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        serviceRequest.setStatus(ServiceRequest.Status.REJECTED);
        serviceRequest.setRejectionReason(rejectionReason);
        serviceRequest.setRejectedDate(LocalDateTime.now());
        serviceRequest.setRejectedBy(currentUser);
        serviceRequest.setUpdatedBy(currentUser);
        serviceRequest.setUpdatedAt(LocalDateTime.now());

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        // TODO: Send notification to client
        // try {
        // notificationService.notifyServiceRequestRejected(savedRequest.getId(),
        // savedRequest.getClient().getId());
        // } catch (Exception e) {
        // log.error("Failed to send notification for service request rejection: {}",
        // e.getMessage());
        // }

        return convertToDTO(savedRequest);
    }

    @Override
    public ServiceRequestDTO updateServiceRequestStatus(Long requestId, ServiceRequest.Status status) {

        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        User currentUser = getCurrentUser();

        // Check if user has permission to update status
        if (currentUser.getRole() == User.Role.CLIENT) {

            // Clients can only cancel their own service requests
            if (status != ServiceRequest.Status.CANCELLED) {

                throw new RuntimeException("Clients can only cancel their own service requests");
            }

            // Check if the service request belongs to the current client
            Client client = getClientForUser(currentUser);

            if (client == null || !client.getId().equals(serviceRequest.getClient().getId())) {

                throw new RuntimeException("You can only cancel your own service requests");
            }

            // Check if the service request is in a cancellable state

            if (serviceRequest.getStatus() != ServiceRequest.Status.PENDING &&
                    serviceRequest.getStatus() != ServiceRequest.Status.ASSIGNED) {

                throw new RuntimeException("Service request cannot be cancelled in its current state");
            }

        }

        serviceRequest.setStatus(status);
        serviceRequest.setUpdatedBy(currentUser);
        serviceRequest.setUpdatedAt(LocalDateTime.now());

        // Set completion date if status is COMPLETED
        if (status == ServiceRequest.Status.COMPLETED) {
            serviceRequest.setCompletedDate(LocalDateTime.now());
        }

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        return convertToDTO(savedRequest);
    }

    @Override
    public ServiceRequestDTO acceptServiceRequest(Long requestId, String staffNotes) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.STAFF) {
            throw new RuntimeException("Only staff can accept service requests");
        }

        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        Staff staff = getCurrentStaff();
        if (staff == null) {
            throw new RuntimeException("Staff not found for current user");
        }

        // Check if the service request is assigned to this staff member
        if (!staff.getId().equals(serviceRequest.getAssignedStaff().getId())) {
            throw new RuntimeException("Service request is not assigned to you");
        }

        serviceRequest.setStatus(ServiceRequest.Status.IN_PROGRESS);
        serviceRequest.setAcceptedBy(currentUser);
        serviceRequest.setStaffNotes(staffNotes);
        serviceRequest.setUpdatedBy(currentUser);
        serviceRequest.setUpdatedAt(LocalDateTime.now());

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        // TODO: Send notification to client
        // try {
        // notificationService.notifyServiceRequestAccepted(savedRequest.getId(),
        // savedRequest.getClient().getId());
        // } catch (Exception e) {
        // log.error("Failed to send notification for service request acceptance: {}",
        // e.getMessage());
        // }

        return convertToDTO(savedRequest);
    }

    @Override
    public ServiceRequestDTO convertServiceRequestToTask(Long requestId, TaskConversionDTO taskData) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.STAFF && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only staff and admin can convert service requests to tasks");
        }

        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        Staff assignedStaff = null;
        if (currentUser.getRole() == User.Role.STAFF) {
            // Staff user - check if assigned to this service request
            Staff staff = getCurrentStaff();
            if (staff == null) {
                throw new RuntimeException("Staff not found for current user");
            }
            if (!staff.getId().equals(serviceRequest.getAssignedStaff().getId())) {
                throw new RuntimeException("Service request is not assigned to you");
            }
            assignedStaff = staff;
        } else if (currentUser.getRole() == User.Role.ADMIN) {
            // Admin user - can assign to any staff or use the currently assigned staff
            if (serviceRequest.getAssignedStaff() != null) {
                assignedStaff = serviceRequest.getAssignedStaff();
            } else {
                throw new RuntimeException("Service request must be assigned to staff before converting to task");
            }
        }

        // Get current timestamp in UTC to avoid timezone conversion issues
        // This ensures consistent time storage regardless of server timezone
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // Create a new task from the service request
        Task task = Task.builder()
                .title(taskData.getTitle())
                .description(taskData.getDescription())
                .client(serviceRequest.getClient())
                .assignedStaff(assignedStaff)
                .taskType(Task.TaskType.OTHER) // Default type, can be enhanced
                .status(Task.Status.PENDING)
                .priority(taskData.getPriority() != null
                        ? convertPriority(ServiceRequest.Priority.valueOf(taskData.getPriority()))
                        : convertPriority(serviceRequest.getPriority()))
                .dueDate(taskData.getDueDate() != null ? taskData.getDueDate() : serviceRequest.getPreferredDeadline())
                .estimatedHours(
                        taskData.getEstimatedHours() != null ? taskData.getEstimatedHours().doubleValue() : null)
                .assignedDate(now)
                .createdBy(currentUser)
                .serviceItem(serviceRequest.getServiceItem())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Task savedTask = taskRepository.save(task);

        // Update service request status to completed
        serviceRequest.setStatus(ServiceRequest.Status.COMPLETED);
        serviceRequest.setCompletedDate(now);
        serviceRequest.setUpdatedBy(currentUser);
        serviceRequest.setUpdatedAt(now);

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        // TODO: Send notification to client
        // try {
        // notificationService.notifyServiceRequestConvertedToTask(savedRequest.getId(),
        // savedTask.getId(), savedRequest.getClient().getId());
        // } catch (Exception e) {
        // log.error("Failed to send notification for service request conversion: {}",
        // e.getMessage());
        // }

        return convertToDTO(savedRequest);
    }

    @Override
    public Map<String, Object> getServiceRequestStatistics() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admins can view service request statistics");
        }

        Map<String, Object> statistics = new HashMap<>();

        // Get counts for each status
        statistics.put("totalRequests", serviceRequestRepository.count());
        statistics.put("pendingRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.PENDING));
        statistics.put("assignedRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.ASSIGNED));
        statistics.put("inProgressRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.IN_PROGRESS));
        statistics.put("completedRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.COMPLETED));
        statistics.put("rejectedRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.REJECTED));
        statistics.put("cancelledRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.CANCELLED));

        // Get overdue requests count
        LocalDate today = LocalDate.now();
        Page<ServiceRequest> overdueRequests = serviceRequestRepository.findOverdueRequests(today, Pageable.unpaged());
        statistics.put("overdueRequests", overdueRequests.getTotalElements());

        return statistics;
    }

    @Override
    public Map<String, Object> getClientServiceRequestStatistics(Long clientId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN && currentUser.getRole() != User.Role.CLIENT) {
            throw new RuntimeException("Only admins and clients can view client service request statistics");
        }

        // If client, verify they're viewing their own statistics
        if (currentUser.getRole() == User.Role.CLIENT) {
            Client client = getClientForUser(currentUser);
            if (client == null || !client.getId().equals(clientId)) {
                throw new RuntimeException("Access denied to client service request statistics");
            }
        }

        Map<String, Object> statistics = new HashMap<>();

        // Get counts for each status for this client
        statistics.put("totalRequests", serviceRequestRepository.countByClientIdAndStatus(clientId, null));
        statistics.put("pendingRequests",
                serviceRequestRepository.countByClientIdAndStatus(clientId, ServiceRequest.Status.PENDING));
        statistics.put("assignedRequests",
                serviceRequestRepository.countByClientIdAndStatus(clientId, ServiceRequest.Status.ASSIGNED));
        statistics.put("inProgressRequests",
                serviceRequestRepository.countByClientIdAndStatus(clientId, ServiceRequest.Status.IN_PROGRESS));
        statistics.put("completedRequests",
                serviceRequestRepository.countByClientIdAndStatus(clientId, ServiceRequest.Status.COMPLETED));
        statistics.put("rejectedRequests",
                serviceRequestRepository.countByClientIdAndStatus(clientId, ServiceRequest.Status.REJECTED));
        statistics.put("cancelledRequests",
                serviceRequestRepository.countByClientIdAndStatus(clientId, ServiceRequest.Status.CANCELLED));

        return statistics;
    }

    @Override
    public Map<String, Object> getStaffServiceRequestStatistics(Long staffId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN && currentUser.getRole() != User.Role.STAFF) {
            throw new RuntimeException("Only admins and staff can view staff service request statistics");
        }

        // If staff, verify they're viewing their own statistics
        if (currentUser.getRole() == User.Role.STAFF) {
            Staff staff = getCurrentStaff();
            if (staff == null || !staff.getId().equals(staffId)) {
                throw new RuntimeException("Access denied to staff service request statistics");
            }
        }

        Map<String, Object> statistics = new HashMap<>();

        // Get counts for each status for this staff
        statistics.put("totalRequests", serviceRequestRepository.countByAssignedStaffIdAndStatus(staffId, null));
        statistics.put("pendingRequests",
                serviceRequestRepository.countByAssignedStaffIdAndStatus(staffId, ServiceRequest.Status.PENDING));
        statistics.put("assignedRequests",
                serviceRequestRepository.countByAssignedStaffIdAndStatus(staffId, ServiceRequest.Status.ASSIGNED));
        statistics.put("inProgressRequests",
                serviceRequestRepository.countByAssignedStaffIdAndStatus(staffId, ServiceRequest.Status.IN_PROGRESS));
        statistics.put("completedRequests",
                serviceRequestRepository.countByAssignedStaffIdAndStatus(staffId, ServiceRequest.Status.COMPLETED));
        statistics.put("rejectedRequests",
                serviceRequestRepository.countByAssignedStaffIdAndStatus(staffId, ServiceRequest.Status.REJECTED));
        statistics.put("cancelledRequests",
                serviceRequestRepository.countByAssignedStaffIdAndStatus(staffId, ServiceRequest.Status.CANCELLED));

        return statistics;
    }

    @Override
    public boolean canEditServiceRequest(Long requestId) {
        try {
            ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                    .orElse(null);
            if (serviceRequest == null) {
                return false;
            }
            return hasAccessToServiceRequest(serviceRequest);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canDeleteServiceRequest(Long requestId) {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }

    @Override
    public boolean canAssignServiceRequest(Long requestId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
                return false;
            }

            ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                    .orElse(null);
            if (serviceRequest == null) {
                return false;
            }

            // Admin can assign if status is PENDING
            return serviceRequest.getStatus() == ServiceRequest.Status.PENDING;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canRejectServiceRequest(Long requestId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
                return false;
            }

            ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                    .orElse(null);
            if (serviceRequest == null) {
                return false;
            }

            // Admin can reject if status is PENDING
            return serviceRequest.getStatus() == ServiceRequest.Status.PENDING;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canAcceptServiceRequest(Long requestId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != User.Role.STAFF) {
                return false;
            }

            ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                    .orElse(null);
            if (serviceRequest == null) {
                return false;
            }

            Staff staff = getCurrentStaff();
            if (staff == null) {
                return false;
            }

            // Staff can accept if assigned to them and status is ASSIGNED
            return serviceRequest.getAssignedStaff() != null &&
                    serviceRequest.getAssignedStaff().getId().equals(staff.getId()) &&
                    serviceRequest.getStatus() == ServiceRequest.Status.ASSIGNED;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canConvertToTask(Long requestId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != User.Role.STAFF) {
                return false;
            }

            ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                    .orElse(null);
            if (serviceRequest == null) {
                return false;
            }

            Staff staff = getCurrentStaff();
            if (staff == null) {
                return false;
            }

            // Staff can convert if assigned to them and status is IN_PROGRESS
            return serviceRequest.getAssignedStaff() != null &&
                    serviceRequest.getAssignedStaff().getId().equals(staff.getId()) &&
                    serviceRequest.getStatus() == ServiceRequest.Status.IN_PROGRESS;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to convert ServiceRequest entity to DetailDTO
    private ServiceRequestDetailDTO convertToDetailDTO(ServiceRequest serviceRequest) {
        ServiceRequestDetailDTO detailDTO = new ServiceRequestDetailDTO();

        // Copy all basic fields from convertToDTO
        ServiceRequestDTO basicDTO = convertToDTO(serviceRequest);

        // Set basic fields
        detailDTO.setId(basicDTO.getId());
        detailDTO.setRequestId(basicDTO.getRequestId());
        detailDTO.setServiceCategoryName(basicDTO.getServiceCategoryName());
        detailDTO.setServiceSubcategoryName(basicDTO.getServiceSubcategoryName());
        detailDTO.setServiceItemId(basicDTO.getServiceItemId());
        detailDTO.setServiceItemName(basicDTO.getServiceItemName());
        detailDTO.setDescription(basicDTO.getDescription());
        detailDTO.setNotes(basicDTO.getNotes());
        detailDTO.setPreferredDeadline(basicDTO.getPreferredDeadline());
        detailDTO.setPriority(basicDTO.getPriority());
        detailDTO.setStatus(basicDTO.getStatus());
        detailDTO.setRejectionReason(basicDTO.getRejectionReason());
        detailDTO.setAdminNotes(basicDTO.getAdminNotes());
        detailDTO.setStaffNotes(basicDTO.getStaffNotes());
        detailDTO.setEstimatedPrice(basicDTO.getEstimatedPrice());
        detailDTO.setFinalPrice(basicDTO.getFinalPrice());
        detailDTO.setAssignedDate(basicDTO.getAssignedDate());
        detailDTO.setCompletedDate(basicDTO.getCompletedDate());
        detailDTO.setRejectedDate(basicDTO.getRejectedDate());
        detailDTO.setClientId(basicDTO.getClientId());
        detailDTO.setClientName(basicDTO.getClientName());
        detailDTO.setClientEmail(basicDTO.getClientEmail());
        detailDTO.setClientPhone(basicDTO.getClientPhone());
        detailDTO.setCompanyName(basicDTO.getCompanyName());
        detailDTO.setAssignedStaffId(basicDTO.getAssignedStaffId());
        detailDTO.setAssignedStaffName(basicDTO.getAssignedStaffName());
        detailDTO.setAssignedStaffEmail(basicDTO.getAssignedStaffEmail());
        detailDTO.setAssignedStaffEmployeeId(basicDTO.getAssignedStaffEmployeeId());
        detailDTO.setAcceptedById(basicDTO.getAcceptedById());
        detailDTO.setAcceptedByName(basicDTO.getAcceptedByName());
        detailDTO.setAssignedById(basicDTO.getAssignedById());
        detailDTO.setAssignedByName(basicDTO.getAssignedByName());
        detailDTO.setRejectedById(basicDTO.getRejectedById());
        detailDTO.setRejectedByName(basicDTO.getRejectedByName());
        detailDTO.setCreatedById(basicDTO.getCreatedById());
        detailDTO.setCreatedByName(basicDTO.getCreatedByName());
        detailDTO.setUpdatedById(basicDTO.getUpdatedById());
        detailDTO.setUpdatedByName(basicDTO.getUpdatedByName());
        detailDTO.setCreatedAt(basicDTO.getCreatedAt());
        detailDTO.setUpdatedAt(basicDTO.getUpdatedAt());

        // Set additional detail fields
        if (serviceRequest.getClient() != null) {
            detailDTO.setClientAddress(serviceRequest.getClient().getAddress());
            detailDTO.setClientCity(serviceRequest.getClient().getCity());
            detailDTO.setClientState(serviceRequest.getClient().getState());
            detailDTO.setClientPincode(serviceRequest.getClient().getPincode());
            detailDTO.setClientGstNumber(serviceRequest.getClient().getGstNumber());
            detailDTO.setClientPanNumber(serviceRequest.getClient().getPanNumber());
        }

        if (serviceRequest.getAssignedStaff() != null) {
            detailDTO.setAssignedStaffPhone(serviceRequest.getAssignedStaff().getUser().getPhone());
            detailDTO.setAssignedStaffPosition(serviceRequest.getAssignedStaff().getPosition());
            detailDTO.setAssignedStaffDepartment(serviceRequest.getAssignedStaff().getDepartment());
        }

        // Set email fields for users
        if (serviceRequest.getAcceptedBy() != null) {
            detailDTO.setAcceptedByEmail(serviceRequest.getAcceptedBy().getEmail());
        }
        if (serviceRequest.getAssignedBy() != null) {
            detailDTO.setAssignedByEmail(serviceRequest.getAssignedBy().getEmail());
        }
        if (serviceRequest.getRejectedBy() != null) {
            detailDTO.setRejectedByEmail(serviceRequest.getRejectedBy().getEmail());
        }
        if (serviceRequest.getCreatedBy() != null) {
            detailDTO.setCreatedByEmail(serviceRequest.getCreatedBy().getEmail());
        }
        if (serviceRequest.getUpdatedBy() != null) {
            detailDTO.setUpdatedByEmail(serviceRequest.getUpdatedBy().getEmail());
        }

        // Set badge classes
        detailDTO.setStatusBadgeClass(getStatusBadgeClass(serviceRequest.getStatus().toString()));
        detailDTO.setPriorityBadgeClass(getPriorityBadgeClass(serviceRequest.getPriority().toString()));

        return detailDTO;
    }

    // Helper methods for badge classes
    private String getStatusBadgeClass(String status) {
        switch (status) {
            case "PENDING":
                return "bg-yellow-100 text-yellow-800";
            case "ASSIGNED":
                return "bg-blue-100 text-blue-800";
            case "IN_PROGRESS":
                return "bg-orange-100 text-orange-800";
            case "COMPLETED":
                return "bg-green-100 text-green-800";
            case "CANCELLED":
                return "bg-gray-100 text-gray-800";
            case "REJECTED":
                return "bg-red-100 text-red-800";
            default:
                return "bg-gray-100 text-gray-800";
        }
    }

    private String getPriorityBadgeClass(String priority) {
        switch (priority) {
            case "LOW":
                return "bg-green-100 text-green-800";
            case "MEDIUM":
                return "bg-yellow-100 text-yellow-800";
            case "HIGH":
                return "bg-orange-100 text-orange-800";
            case "URGENT":
                return "bg-red-100 text-red-800";
            default:
                return "bg-gray-100 text-gray-800";
        }
    }

    // Helper method to convert ServiceRequest entity to DTO
    private ServiceRequestDTO convertToDTO(ServiceRequest serviceRequest) {
        return ServiceRequestDTO.builder()
                .id(serviceRequest.getId())
                .requestId(serviceRequest.getRequestId())
                .serviceCategoryName(serviceRequest.getServiceCategoryName())
                .serviceSubcategoryName(serviceRequest.getServiceSubcategoryName())
                .serviceItemId(serviceRequest.getServiceItem() != null ? serviceRequest.getServiceItem().getId() : null)
                .serviceItemName(serviceRequest.getServiceItemName())
                .description(serviceRequest.getDescription())
                .notes(serviceRequest.getNotes())
                .preferredDeadline(serviceRequest.getPreferredDeadline())
                .priority(serviceRequest.getPriority())
                .status(serviceRequest.getStatus())
                .rejectionReason(serviceRequest.getRejectionReason())
                .adminNotes(serviceRequest.getAdminNotes())
                .staffNotes(serviceRequest.getStaffNotes())
                .estimatedPrice(serviceRequest.getEstimatedPrice())
                .finalPrice(serviceRequest.getFinalPrice())
                .assignedDate(serviceRequest.getAssignedDate())
                .completedDate(serviceRequest.getCompletedDate())
                .rejectedDate(serviceRequest.getRejectedDate())
                .clientId(serviceRequest.getClient().getId())
                .clientName(serviceRequest.getClient().getUser().getFirstName() + " "
                        + serviceRequest.getClient().getUser().getLastName())
                .clientEmail(serviceRequest.getClient().getUser().getEmail())
                .clientPhone(serviceRequest.getClient().getUser().getPhone())
                .companyName(serviceRequest.getClient().getCompanyName())
                .assignedStaffId(
                        serviceRequest.getAssignedStaff() != null ? serviceRequest.getAssignedStaff().getId() : null)
                .assignedStaffName(
                        serviceRequest.getAssignedStaff() != null
                                ? serviceRequest.getAssignedStaff().getUser().getFirstName() + " "
                                        + serviceRequest.getAssignedStaff().getUser().getLastName()
                                : null)
                .assignedStaffEmail(serviceRequest.getAssignedStaff() != null
                        ? serviceRequest.getAssignedStaff().getUser().getEmail()
                        : null)
                .assignedStaffEmployeeId(
                        serviceRequest.getAssignedStaff() != null ? serviceRequest.getAssignedStaff().getEmployeeId()
                                : null)
                .acceptedById(serviceRequest.getAcceptedBy() != null ? serviceRequest.getAcceptedBy().getId() : null)
                .acceptedByName(serviceRequest.getAcceptedBy() != null ? serviceRequest.getAcceptedBy().getFirstName()
                        + " " + serviceRequest.getAcceptedBy().getLastName() : null)
                .assignedById(serviceRequest.getAssignedBy() != null ? serviceRequest.getAssignedBy().getId() : null)
                .assignedByName(serviceRequest.getAssignedBy() != null ? serviceRequest.getAssignedBy().getFirstName()
                        + " " + serviceRequest.getAssignedBy().getLastName() : null)
                .rejectedById(serviceRequest.getRejectedBy() != null ? serviceRequest.getRejectedBy().getId() : null)
                .rejectedByName(serviceRequest.getRejectedBy() != null ? serviceRequest.getRejectedBy().getFirstName()
                        + " " + serviceRequest.getRejectedBy().getLastName() : null)
                .createdById(serviceRequest.getCreatedBy().getId())
                .createdByName(serviceRequest.getCreatedBy().getFirstName() + " "
                        + serviceRequest.getCreatedBy().getLastName())
                .updatedById(serviceRequest.getUpdatedBy() != null ? serviceRequest.getUpdatedBy().getId() : null)
                .updatedByName(serviceRequest.getUpdatedBy() != null ? serviceRequest.getUpdatedBy().getFirstName()
                        + " " + serviceRequest.getUpdatedBy().getLastName() : null)
                .createdAt(serviceRequest.getCreatedAt())
                .updatedAt(serviceRequest.getUpdatedAt())
                .build();
    }
}
