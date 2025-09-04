package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.LeadDTO;
import com.adspeek.authservice.dto.PublicLeadRequest;
import com.adspeek.authservice.dto.ClientDTO;
import com.adspeek.authservice.entity.*;
import com.adspeek.authservice.repository.LeadRepository;
import com.adspeek.authservice.repository.LeadActivityRepository;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.service.LeadService;
import com.adspeek.authservice.service.NotificationService;
import com.adspeek.authservice.service.ClientService;
import com.adspeek.authservice.service.IdGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final LeadActivityRepository leadActivityRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ClientService clientService;
    private final IdGenerationService idGenerationService;

    @Override
    public LeadDTO createLead(LeadDTO leadDTO) {
        Lead lead = convertToEntity(leadDTO);
        lead.setCreatedBy(getCurrentUser());

        // Generate sequential lead ID
        String leadId = idGenerationService.generateNextLeadId();
        lead.setLeadId(leadId);

        Lead savedLead = leadRepository.save(lead);

        // Log activity
        logLeadActivity(savedLead, LeadActivity.ActivityType.LEAD_CREATED, "Lead created", null, null);

        return convertToDTO(savedLead);
    }

    @Override
    public LeadDTO createPublicLead(PublicLeadRequest request) {
        // Generate sequential lead ID
        String leadId = idGenerationService.generateNextLeadId();

        Lead lead = Lead.builder()
                .leadId(leadId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .companyName(request.getCompanyName())
                .serviceItemId(request.getServiceItemId())
                .serviceCategoryName(request.getServiceCategoryName())
                .serviceSubcategoryName(request.getServiceSubcategoryName())
                .serviceItemName(request.getServiceItemName())
                .serviceDescription(request.getServiceDescription())
                .source(request.getSource() != null ? request.getSource() : Lead.Source.WEBSITE)
                .status(Lead.Status.NEW)
                .priority(Lead.Priority.MEDIUM)
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Lead savedLead = leadRepository.save(lead);

        // Log activity for public lead (no authenticated user)
        logPublicLeadActivity(savedLead, LeadActivity.ActivityType.LEAD_CREATED, "Public lead captured", null, null);

        // Send notification to admin users about new lead
        notificationService.notifyNewLeadCreatedForAdmin(savedLead.getId());

        return convertToDTO(savedLead);
    }

    @Override
    public LeadDTO updateLead(Long id, LeadDTO leadDTO) {
        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        Lead.Status oldStatus = existingLead.getStatus();
        Long oldAssignedStaffId = existingLead.getAssignedStaff() != null ? existingLead.getAssignedStaff().getId()
                : null;

        updateLeadFields(existingLead, leadDTO);
        existingLead.setUpdatedBy(getCurrentUser());
        existingLead.setUpdatedAt(LocalDateTime.now());

        Lead savedLead = leadRepository.save(existingLead);

        // Log status change if status changed
        if (oldStatus != savedLead.getStatus()) {
            logLeadActivity(savedLead, LeadActivity.ActivityType.STATUS_CHANGED,
                    "Status changed from " + oldStatus + " to " + savedLead.getStatus(),
                    oldStatus.toString(), savedLead.getStatus().toString());

            // Auto-convert lead to client when status is set to CONVERTED
            if (savedLead.getStatus() == Lead.Status.CONVERTED) {
                try {
                    convertLeadToClientAutomatically(savedLead);

                } catch (Exception e) {
                    log.error("Failed to automatically convert lead {} to client: {}", savedLead.getId(),
                            e.getMessage());
                    // Don't throw exception to avoid breaking the lead update
                }
            }
        } else {
            // Check if lead is CONVERTED but no client exists (manual conversion needed)
            if (savedLead.getStatus() == Lead.Status.CONVERTED) {
                try {
                    User existingUser = userRepository.findByEmail(savedLead.getEmail()).orElse(null);
                    if (existingUser == null || existingUser.getRole() != User.Role.CLIENT) {
                        convertLeadToClientAutomatically(savedLead);
                    }
                } catch (Exception e) {
                    log.error("Failed to automatically convert lead {} to client via admin update: {}",
                            savedLead.getId(),
                            e.getMessage());
                    // Don't let conversion failure affect the main update
                }
            }
        }

        // Log assignment change if assigned staff changed
        Long newAssignedStaffId = savedLead.getAssignedStaff() != null ? savedLead.getAssignedStaff().getId() : null;
        if (!java.util.Objects.equals(oldAssignedStaffId, newAssignedStaffId)) {
            logLeadActivity(savedLead, LeadActivity.ActivityType.ASSIGNED,
                    "Lead assigned to staff",
                    oldAssignedStaffId != null ? oldAssignedStaffId.toString() : "Unassigned",
                    newAssignedStaffId != null ? newAssignedStaffId.toString() : "Unassigned");
        }

        return convertToDTO(savedLead);
    }

    @Override
    public LeadDTO updateLeadByStaff(Long id, LeadDTO leadDTO) {
        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        // Get current staff user
        User currentUser = getCurrentUser();
        Staff currentStaff = staffRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Staff can update specific fields including status:
        // - Status (to allow conversion)
        // - Next Follow-up Date
        // - Notes
        // - Service Required
        // - Service Description
        // - Source
        // - Estimated Value
        // - Last Contact Date
        if (leadDTO.getStatus() != null) {
            existingLead.setStatus(leadDTO.getStatus());
        }
        if (leadDTO.getNextFollowUpDate() != null) {
            existingLead.setNextFollowUpDate(leadDTO.getNextFollowUpDate());
        }
        if (leadDTO.getNotes() != null) {
            existingLead.setNotes(leadDTO.getNotes());
        }
        // Service hierarchy fields are handled in updateLeadFields method
        if (leadDTO.getServiceDescription() != null) {
            existingLead.setServiceDescription(leadDTO.getServiceDescription());
        }
        if (leadDTO.getSource() != null) {
            existingLead.setSource(leadDTO.getSource());
        }
        if (leadDTO.getEstimatedValue() != null) {
            existingLead.setEstimatedValue(leadDTO.getEstimatedValue());
        }
        if (leadDTO.getLastContactDate() != null) {
            existingLead.setLastContactDate(leadDTO.getLastContactDate());
        }

        Lead.Status oldStatus = existingLead.getStatus();

        existingLead.setUpdatedBy(getCurrentUser());
        existingLead.setUpdatedAt(LocalDateTime.now());

        Lead savedLead = leadRepository.save(existingLead);

        // Log status change if status changed
        if (oldStatus != savedLead.getStatus()) {
            logLeadActivity(savedLead, LeadActivity.ActivityType.STATUS_CHANGED,
                    "Status changed from " + oldStatus + " to " + savedLead.getStatus(),
                    oldStatus.toString(), savedLead.getStatus().toString());

            // Auto-convert lead to client when status is set to CONVERTED
            if (savedLead.getStatus() == Lead.Status.CONVERTED) {
                try {
                    convertLeadToClientAutomatically(savedLead);
                } catch (Exception e) {
                    log.error("Failed to automatically convert lead {} to client: {}", savedLead.getId(),
                            e.getMessage());
                    // Don't throw exception to avoid breaking the lead update
                }
            }
        } else {
            // Check if lead is CONVERTED but no client exists (manual conversion needed)
            if (savedLead.getStatus() == Lead.Status.CONVERTED) {
                try {
                    User existingUser = userRepository.findByEmail(savedLead.getEmail()).orElse(null);
                    if (existingUser == null || existingUser.getRole() != User.Role.CLIENT) {
                        convertLeadToClientAutomatically(savedLead);
                    }
                } catch (Exception e) {
                    log.error("Failed to automatically convert lead {} to client via staff update: {}",
                            savedLead.getId(),
                            e.getMessage());
                    // Don't let conversion failure affect the main update
                }
            }
        }

        return convertToDTO(savedLead);
    }

    @Override
    public LeadDTO getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return convertToDTO(lead);
    }

    @Override
    public LeadDTO getLeadByLeadId(String leadId) {
        Lead lead = leadRepository.findByLeadId(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return convertToDTO(lead);
    }

    @Override
    public Page<LeadDTO> getAllLeads(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leadRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public LeadDTO assignLeadToStaff(Long leadId, Long staffId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        Long oldAssignedStaffId = lead.getAssignedStaff() != null ? lead.getAssignedStaff().getId() : null;

        lead.setAssignedStaff(staff);
        lead.setUpdatedBy(getCurrentUser());
        lead.setUpdatedAt(LocalDateTime.now());

        Lead savedLead = leadRepository.save(lead);

        // Log assignment
        logLeadActivity(savedLead, LeadActivity.ActivityType.ASSIGNED,
                "Lead assigned to " + staff.getUser().getFirstName() + " " + staff.getUser().getLastName(),
                oldAssignedStaffId != null ? oldAssignedStaffId.toString() : "Unassigned",
                staffId.toString());

        // Send notification to assigned staff
        notificationService.notifyLeadAssigned(savedLead.getId(), staffId);

        return convertToDTO(savedLead);
    }

    @Override
    public LeadDTO updateLeadStatus(Long leadId, Lead.Status status, String reason) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        Lead.Status oldStatus = lead.getStatus();
        lead.setStatus(status);

        if (status == Lead.Status.LOST) {
            lead.setLostReason(reason);
        } else if (status == Lead.Status.CONVERTED) {
            lead.setConvertedDate(LocalDateTime.now());
        }

        lead.setUpdatedBy(getCurrentUser());
        lead.setUpdatedAt(LocalDateTime.now());

        Lead savedLead = leadRepository.save(lead);

        // Log status change
        logLeadActivity(savedLead, LeadActivity.ActivityType.STATUS_CHANGED,
                "Status changed to " + status + (reason != null ? " - " + reason : ""),
                oldStatus.toString(), status.toString());

        // Auto-convert lead to client when status is set to CONVERTED
        if (savedLead.getStatus() == Lead.Status.CONVERTED) {
            try {
                convertLeadToClientAutomatically(savedLead);
            } catch (Exception e) {
                log.error("Failed to automatically convert lead {} to client via updateLeadStatus: {}",
                        savedLead.getId(),
                        e.getMessage());
                // Don't let conversion failure affect the status update
            }
        }

        return convertToDTO(savedLead);
    }

    @Override
    public LeadDTO convertLeadToClient(Long leadId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        // Update status to CONVERTED which will trigger automatic conversion
        return updateLeadStatus(leadId, Lead.Status.CONVERTED, "Lead converted to client");
    }

    /**
     * Automatically converts a lead to a client when status is set to CONVERTED
     * This method is called internally when lead status changes to CONVERTED
     */
    private void convertLeadToClientAutomatically(Lead lead) {
        // Check if client already exists for this lead by checking if a user with this
        // email exists
        try {
            User existingUser = userRepository.findByEmail(lead.getEmail()).orElse(null);
            if (existingUser != null && existingUser.getRole() == User.Role.CLIENT) {
                return;
            }
        } catch (Exception e) {
            log.warn("Error checking if client exists for lead {}: {}", lead.getId(), e.getMessage());
        }

        try {
            // Create a new user for the client
            User clientUser = User.builder()
                    .firstName(lead.getFirstName())
                    .lastName(lead.getLastName())
                    .email(lead.getEmail())
                    .phone(lead.getPhone())
                    .role(User.Role.CLIENT)
                    .isActive(true)
                    .passwordHash("$2a$10$U1EgKPoUls0Bb4GP0JyCWe4IvlBayph/d83SLZ0Lh4wxxJAQUheD2") // Default password:
                                                                                                  // admin
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            User savedUser = userRepository.save(clientUser);

            // Determine the assigned staff for the client
            Long assignedStaffId = null;
            User currentUser = getCurrentUser();

            // Priority order for staff assignment:
            // 1. If lead already has assigned staff, use that
            // 2. If current user is a staff member, assign them
            // 3. Otherwise, leave unassigned (admin can assign later)
            if (lead.getAssignedStaff() != null) {
                assignedStaffId = lead.getAssignedStaff().getId();
                log.info("Using existing assigned staff {} for converted client", assignedStaffId);
            } else if (currentUser.getRole() == User.Role.STAFF) {
                // Get the staff record for the current user
                Staff currentStaff = staffRepository.findByUserId(currentUser.getId()).orElse(null);
                if (currentStaff != null) {
                    assignedStaffId = currentStaff.getId();
                    log.info("Assigning converting staff member {} to converted client", assignedStaffId);
                }
            }

            // Create client DTO from lead data
            ClientDTO clientDTO = ClientDTO.builder()
                    .userId(savedUser.getId())
                    .companyName(lead.getCompanyName())
                    .contactPerson(lead.getFirstName() + " " + lead.getLastName())
                    .contactPhone(lead.getPhone())
                    .contactEmail(lead.getEmail())
                    .clientType(Client.ClientType.INDIVIDUAL) // Default to individual
                    .registrationDate(LocalDate.now())
                    .isActive(true)
                    .assignedStaffId(assignedStaffId)
                    .build();

            // Create the client
            ClientDTO createdClient = clientService.createClient(clientDTO);

            // Update lead with conversion details
            lead.setConvertedDate(LocalDateTime.now());
            lead.setUpdatedBy(currentUser);
            lead.setUpdatedAt(LocalDateTime.now());

            // If we assigned a new staff member during conversion, update the lead as well
            if (assignedStaffId != null && lead.getAssignedStaff() == null) {
                Staff assignedStaff = staffRepository.findById(assignedStaffId).orElse(null);
                if (assignedStaff != null) {
                    lead.setAssignedStaff(assignedStaff);
                }
            }

            leadRepository.save(lead);

            // Log the conversion activity
            String conversionMessage = "Lead automatically converted to client. Client ID: " + createdClient.getId();
            if (assignedStaffId != null) {
                conversionMessage += ". Assigned to staff ID: " + assignedStaffId;
            }

            logLeadActivity(lead, LeadActivity.ActivityType.STATUS_CHANGED,
                    conversionMessage, "LEAD", "CLIENT");

            // Send notification to admin users about lead conversion (non-blocking)
            try {
                notificationService.notifyLeadConvertedToClient(lead.getId(), createdClient.getId());
            } catch (Exception e) {
                log.error("Failed to send notification for lead conversion: {}", e.getMessage());
                // Don't let notification failure affect the main conversion
            }

            // Send notification to staff member if they were assigned during conversion
            // (non-blocking)
            if (assignedStaffId != null) {
                try {
                    notificationService.notifyStaffAssignedToClient(createdClient.getId(), assignedStaffId);
                } catch (Exception e) {
                    log.error("Failed to send staff assignment notification: {}", e.getMessage());
                    // Don't let notification failure affect the main conversion
                }
            }

            log.info("Successfully converted lead {} to client with ID: {}. Assigned staff: {}",
                    lead.getId(), createdClient.getId(), assignedStaffId);

        } catch (Exception e) {
            log.error("Error converting lead {} to client: {}", lead.getId(), e.getMessage());
            throw new RuntimeException("Failed to convert lead to client: " + e.getMessage());
        }
    }

    @Override
    public void deleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        leadRepository.delete(lead);
    }

    @Override
    public List<LeadDTO> getLeadsByFollowUpDate(LocalDate date) {
        return leadRepository.findByNextFollowUpDate(date)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadDTO> getOverdueFollowUps() {
        return leadRepository.findOverdueFollowUps(LocalDate.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getLeadStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count by status
        for (Lead.Status status : Lead.Status.values()) {
            stats.put(status.toString(), leadRepository.countByStatus(status));
        }

        // Count by service required - using service item name instead
        Map<String, Long> serviceCounts = new HashMap<>();
        // For now, we'll use a placeholder since we removed the old service required
        // enum
        // This can be enhanced later to use the new service hierarchy
        stats.put("byService", serviceCounts);

        // Count by source
        List<Object[]> sourceStats = leadRepository.countBySource();
        Map<String, Long> sourceCounts = new HashMap<>();
        for (Object[] stat : sourceStats) {
            sourceCounts.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("bySource", sourceCounts);

        return stats;
    }

    @Override
    public Map<String, Object> getLeadStatisticsByStaff(Long staffId) {
        Map<String, Object> stats = new HashMap<>();

        // Count by status for specific staff
        for (Lead.Status status : Lead.Status.values()) {
            stats.put(status.toString(), leadRepository.countByAssignedStaffIdAndStatus(staffId, status));
        }

        return stats;
    }

    @Override
    public Page<LeadDTO> getRecentLeads(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Lead> leads = leadRepository.findRecentLeads(pageable);
        return leads.map(this::convertToDTO);
    }

    private Lead convertToEntity(LeadDTO dto) {
        Lead lead = new Lead();
        lead.setId(dto.getId());
        lead.setLeadId(dto.getLeadId());
        lead.setFirstName(dto.getFirstName());
        lead.setLastName(dto.getLastName());
        lead.setEmail(dto.getEmail());
        lead.setPhone(dto.getPhone());
        lead.setCompanyName(dto.getCompanyName());
        lead.setServiceItemId(dto.getServiceItemId());
        lead.setServiceCategoryName(dto.getServiceCategoryName());
        lead.setServiceSubcategoryName(dto.getServiceSubcategoryName());
        lead.setServiceItemName(dto.getServiceItemName());
        lead.setServiceDescription(dto.getServiceDescription());
        lead.setSource(dto.getSource());
        lead.setStatus(dto.getStatus());
        lead.setPriority(dto.getPriority());
        lead.setEstimatedValue(dto.getEstimatedValue());
        lead.setNotes(dto.getNotes());
        lead.setNextFollowUpDate(dto.getNextFollowUpDate());
        lead.setLastContactDate(dto.getLastContactDate());
        lead.setConvertedDate(dto.getConvertedDate());
        lead.setLostReason(dto.getLostReason());
        lead.setIpAddress(dto.getIpAddress());
        lead.setUserAgent(dto.getUserAgent());
        lead.setCreatedAt(dto.getCreatedAt());
        lead.setUpdatedAt(dto.getUpdatedAt());

        if (dto.getAssignedStaffId() != null) {
            Staff staff = staffRepository.findById(dto.getAssignedStaffId()).orElse(null);
            lead.setAssignedStaff(staff);
        }

        if (dto.getCreatedById() != null) {
            User createdBy = userRepository.findById(dto.getCreatedById()).orElse(null);
            lead.setCreatedBy(createdBy);
        }

        if (dto.getUpdatedById() != null) {
            User updatedBy = userRepository.findById(dto.getUpdatedById()).orElse(null);
            lead.setUpdatedBy(updatedBy);
        }

        return lead;
    }

    private LeadDTO convertToDTO(Lead lead) {
        return LeadDTO.builder()
                .id(lead.getId())
                .leadId(lead.getLeadId())
                .firstName(lead.getFirstName())
                .lastName(lead.getLastName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .companyName(lead.getCompanyName())
                .serviceItemId(lead.getServiceItemId())
                .serviceCategoryName(lead.getServiceCategoryName())
                .serviceSubcategoryName(lead.getServiceSubcategoryName())
                .serviceItemName(lead.getServiceItemName())
                .serviceDescription(lead.getServiceDescription())
                .source(lead.getSource())
                .status(lead.getStatus())
                .priority(lead.getPriority())
                .assignedStaffId(lead.getAssignedStaff() != null ? lead.getAssignedStaff().getId() : null)
                .assignedStaffName(
                        lead.getAssignedStaff() != null
                                ? lead.getAssignedStaff().getUser().getFirstName() + " "
                                        + lead.getAssignedStaff().getUser().getLastName()
                                : null)
                .assignedStaffEmail(
                        lead.getAssignedStaff() != null && lead.getAssignedStaff().getUser() != null
                                ? lead.getAssignedStaff().getUser().getEmail()
                                : null)
                .assignedStaffPhone(
                        lead.getAssignedStaff() != null && lead.getAssignedStaff().getUser() != null
                                ? lead.getAssignedStaff().getUser().getPhone()
                                : null)
                .estimatedValue(lead.getEstimatedValue())
                .notes(lead.getNotes())
                .nextFollowUpDate(lead.getNextFollowUpDate())
                .lastContactDate(lead.getLastContactDate())
                .convertedDate(lead.getConvertedDate())
                .lostReason(lead.getLostReason())
                .ipAddress(lead.getIpAddress())
                .userAgent(lead.getUserAgent())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .createdById(lead.getCreatedBy() != null ? lead.getCreatedBy().getId() : null)
                .createdByName(lead.getCreatedBy() != null
                        ? lead.getCreatedBy().getFirstName() + " " + lead.getCreatedBy().getLastName()
                        : null)
                .updatedById(lead.getUpdatedBy() != null ? lead.getUpdatedBy().getId() : null)
                .updatedByName(lead.getUpdatedBy() != null
                        ? lead.getUpdatedBy().getFirstName() + " " + lead.getUpdatedBy().getLastName()
                        : null)
                .build();
    }

    private void updateLeadFields(Lead lead, LeadDTO dto) {
        if (dto.getFirstName() != null)
            lead.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            lead.setLastName(dto.getLastName());
        if (dto.getEmail() != null)
            lead.setEmail(dto.getEmail());
        if (dto.getPhone() != null)
            lead.setPhone(dto.getPhone());
        if (dto.getCompanyName() != null)
            lead.setCompanyName(dto.getCompanyName());
        if (dto.getServiceItemId() != null)
            lead.setServiceItemId(dto.getServiceItemId());
        if (dto.getServiceCategoryName() != null)
            lead.setServiceCategoryName(dto.getServiceCategoryName());
        if (dto.getServiceSubcategoryName() != null)
            lead.setServiceSubcategoryName(dto.getServiceSubcategoryName());
        if (dto.getServiceItemName() != null)
            lead.setServiceItemName(dto.getServiceItemName());
        if (dto.getServiceDescription() != null)
            lead.setServiceDescription(dto.getServiceDescription());
        if (dto.getSource() != null)
            lead.setSource(dto.getSource());
        if (dto.getStatus() != null)
            lead.setStatus(dto.getStatus());
        if (dto.getPriority() != null)
            lead.setPriority(dto.getPriority());
        if (dto.getEstimatedValue() != null)
            lead.setEstimatedValue(dto.getEstimatedValue());
        if (dto.getNotes() != null)
            lead.setNotes(dto.getNotes());
        if (dto.getNextFollowUpDate() != null)
            lead.setNextFollowUpDate(dto.getNextFollowUpDate());
        if (dto.getLastContactDate() != null)
            lead.setLastContactDate(dto.getLastContactDate());
        if (dto.getConvertedDate() != null)
            lead.setConvertedDate(dto.getConvertedDate());
        if (dto.getLostReason() != null)
            lead.setLostReason(dto.getLostReason());
        if (dto.getIpAddress() != null)
            lead.setIpAddress(dto.getIpAddress());
        if (dto.getUserAgent() != null)
            lead.setUserAgent(dto.getUserAgent());

        if (dto.getAssignedStaffId() != null) {
            Staff staff = staffRepository.findById(dto.getAssignedStaffId()).orElse(null);
            lead.setAssignedStaff(staff);
        }
    }

    private void logLeadActivity(Lead lead, LeadActivity.ActivityType activityType, String description, String oldValue,
            String newValue) {
        LeadActivity activity = LeadActivity.builder()
                .lead(lead)
                .user(getCurrentUser())
                .activityType(activityType)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .createdAt(LocalDateTime.now())
                .build();

        leadActivityRepository.save(activity);
    }

    private void logPublicLeadActivity(Lead lead, LeadActivity.ActivityType activityType, String description,
            String oldValue,
            String newValue) {
        // For public leads, we don't have an authenticated user, so we create activity
        // without user
        LeadActivity activity = LeadActivity.builder()
                .lead(lead)
                .user(null) // No user for public leads
                .activityType(activityType)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .createdAt(LocalDateTime.now())
                .build();

        leadActivityRepository.save(activity);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    @Override
    public Page<LeadDTO> getLeadsWithServiceHierarchyFilters(int page, int size, String search, Lead.Status status,
            Lead.Priority priority, Long serviceCategoryId, Long serviceSubcategoryId, Long serviceItemId) {
        Pageable pageable = PageRequest.of(page, size);

        // Use the repository method that supports service hierarchy filters
        return leadRepository.findByFiltersWithServiceHierarchy(
                search, status, priority, serviceCategoryId, serviceSubcategoryId, serviceItemId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<LeadDTO> getLeadsByAssignedStaffWithServiceHierarchyFilters(Long staffId, int page, int size,
            String search,
            Lead.Status status, Lead.Priority priority, Long serviceCategoryId, Long serviceSubcategoryId,
            Long serviceItemId) {
        Pageable pageable = PageRequest.of(page, size);

        // Use the repository method that supports service hierarchy filters for staff
        // assigned leads
        return leadRepository.findByAssignedStaffWithServiceHierarchyFilters(
                staffId, search, status, priority, serviceCategoryId, serviceSubcategoryId, serviceItemId, pageable)
                .map(this::convertToDTO);
    }
}
