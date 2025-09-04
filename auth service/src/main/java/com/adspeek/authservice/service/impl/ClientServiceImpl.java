package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.ClientDTO;
import com.adspeek.authservice.dto.ClientDetailDTO;
import com.adspeek.authservice.dto.ClientActivityDTO;
import com.adspeek.authservice.dto.ClientDashboardStatsDTO;
import com.adspeek.authservice.entity.Client;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.entity.Staff;
import com.adspeek.authservice.repository.ClientRepository;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.repository.TaskRepository;
import com.adspeek.authservice.service.ClientService;
import com.adspeek.authservice.service.AuditLogService;
import com.adspeek.authservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final TaskRepository taskRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return null;
    }

    private Staff getCurrentStaff() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }

        return staffRepository.findAll().stream()
                .filter(s -> s.getUser() != null && s.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Page<ClientDTO> getAllClients(Pageable pageable, String search, String statusFilter) {
        Page<Client> clients;

        // Check current user's role first
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Page.empty(pageable);
        }

        // If user has ADMIN role, show all clients regardless of staff table entry
        if (currentUser.getRole() == User.Role.ADMIN) {
            // Admin user - show all clients
            // Handle search and status filtering
            if (search != null && !search.trim().isEmpty() && statusFilter != null && !statusFilter.trim().isEmpty()) {
                // Both search and status filter
                Boolean isActive = Boolean.parseBoolean(statusFilter);
                clients = clientRepository.findBySearchTermAndStatus(search.trim(), isActive, pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                // Only search
                clients = clientRepository.findBySearchTerm(search.trim(), pageable);
            } else if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                // Only status filter
                Boolean isActive = Boolean.parseBoolean(statusFilter);
                clients = clientRepository.findByStatus(isActive, pageable);
            } else {
                // No filters
                clients = clientRepository.findAllWithUser(pageable);
            }
        } else {
            // STAFF role - only show assigned clients
            Staff currentStaff = getCurrentStaff();
            if (currentStaff == null) {
                // User is not in staff table, return empty page
                return Page.empty(pageable);
            }

            // Get client IDs from tasks assigned to this staff
            List<Long> taskBasedClientIds = taskRepository.getUniqueClientIdsByStaff(currentStaff.getId());

            // Get client IDs directly assigned to this staff in the client table
            List<Long> directlyAssignedClientIds = clientRepository.findClientIdsByAssignedStaff(currentStaff.getId());

            // Combine both lists and remove duplicates
            List<Long> allAssignedClientIds = new ArrayList<>();
            allAssignedClientIds.addAll(taskBasedClientIds);
            allAssignedClientIds.addAll(directlyAssignedClientIds);

            // Remove duplicates while preserving order
            List<Long> uniqueAssignedClientIds = allAssignedClientIds.stream()
                    .distinct()
                    .collect(Collectors.toList());

            if (uniqueAssignedClientIds.isEmpty()) {
                // No assigned clients, return empty page
                return Page.empty(pageable);
            }

            // Handle search and status filtering for staff
            if (search != null && !search.trim().isEmpty() && statusFilter != null && !statusFilter.trim().isEmpty()) {
                // Both search and status filter
                Boolean isActive = Boolean.parseBoolean(statusFilter);
                clients = clientRepository.findByIdsAndSearchTermAndStatus(uniqueAssignedClientIds, search.trim(),
                        isActive,
                        pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                // Only search
                clients = clientRepository.findByIdsAndSearchTerm(uniqueAssignedClientIds, search.trim(), pageable);
            } else if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                // Only status filter
                Boolean isActive = Boolean.parseBoolean(statusFilter);
                clients = clientRepository.findByIdsAndStatus(uniqueAssignedClientIds, isActive, pageable);
            } else {
                // No filters
                clients = clientRepository.findByIds(uniqueAssignedClientIds, pageable);
            }
        }

        return clients.map(this::convertToDTO);
    }

    @Override
    public Optional<ClientDetailDTO> getClientById(Long id) {
        // Check current user's role first
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Optional.empty();
        }

        // If user has ADMIN role, allow access to all clients
        if (currentUser.getRole() == User.Role.ADMIN) {
            return clientRepository.findById(id).map(this::convertToDetailDTO);
        }

        // CLIENT role - check if they're accessing their own profile
        if (currentUser.getRole() == User.Role.CLIENT) {
            Long currentClientId = getClientIdByUserId(currentUser.getId());
            if (currentClientId != null && currentClientId.equals(id)) {
                return clientRepository.findById(id).map(this::convertToDetailDTO);
            }
            return Optional.empty();
        }

        // STAFF role - check if client is assigned to them
        Staff currentStaff = getCurrentStaff();
        if (currentStaff == null) {
            return Optional.empty();
        }

        // Get client IDs from tasks assigned to this staff
        List<Long> taskBasedClientIds = taskRepository.getUniqueClientIdsByStaff(currentStaff.getId());

        // Get client IDs directly assigned to this staff in the client table
        List<Long> directlyAssignedClientIds = clientRepository.findClientIdsByAssignedStaff(currentStaff.getId());

        // Combine both lists and remove duplicates
        List<Long> allAssignedClientIds = new ArrayList<>();
        allAssignedClientIds.addAll(taskBasedClientIds);
        allAssignedClientIds.addAll(directlyAssignedClientIds);

        // Remove duplicates while preserving order
        List<Long> uniqueAssignedClientIds = allAssignedClientIds.stream()
                .distinct()
                .collect(Collectors.toList());

        if (!uniqueAssignedClientIds.contains(id)) {
            // Client not assigned to this staff member
            return Optional.empty();
        }

        return clientRepository.findById(id).map(this::convertToDetailDTO);
    }

    @Override
    public ClientDTO createClient(ClientDTO clientDTO) {
        User user;

        if (clientDTO.getUserId() != null) {
            // Use existing user
            user = userRepository.findById(clientDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            // Create new user for the client
            user = User.builder()
                    .email(clientDTO.getEmail())
                    .firstName(clientDTO.getFirstName())
                    .lastName(clientDTO.getLastName())
                    .phone(clientDTO.getPhone())
                    .role(User.Role.CLIENT)
                    .isActive(true)
                    .emailVerified(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Set a default password (client will change it later)
            user.setPasswordHash("$2a$10$U1EgKPoUls0Bb4GP0JyCWe4IvlBayph/d83SLZ0Lh4wxxJAQUheD2"); // "admin"
            user = userRepository.save(user);
        }

        // Get assigned staff if provided
        Staff assignedStaff = null;
        if (clientDTO.getAssignedStaffId() != null) {
            assignedStaff = staffRepository.findById(clientDTO.getAssignedStaffId())
                    .orElseThrow(() -> new RuntimeException("Staff not found"));
        }

        // Get current authenticated user for createdBy/updatedBy
        User currentUser = getCurrentUser();

        Client client = Client.builder()
                .user(user)
                .companyName(clientDTO.getCompanyName())
                .companyType(clientDTO.getCompanyType())
                .gstNumber(clientDTO.getGstNumber())
                .panNumber(clientDTO.getPanNumber())
                .address(clientDTO.getAddress())
                .city(clientDTO.getCity())
                .state(clientDTO.getState())
                .pincode(clientDTO.getPincode())
                .country(clientDTO.getCountry())
                .businessType(clientDTO.getBusinessType())
                .industry(clientDTO.getIndustry())
                .website(clientDTO.getWebsite())
                .contactPerson(clientDTO.getContactPerson())
                .contactPhone(clientDTO.getContactPhone())
                .contactEmail(clientDTO.getContactEmail())
                .emergencyContact(clientDTO.getEmergencyContact())
                .clientType(clientDTO.getClientType())
                .registrationDate(clientDTO.getRegistrationDate())
                .assignedStaff(assignedStaff)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        Client savedClient = clientRepository.save(client);
        return convertToDTO(savedClient);
    }

    @Override
    public ClientDTO updateClient(Long id, ClientDTO clientDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Update user information
        User user = client.getUser();
        user.setFirstName(clientDTO.getFirstName());
        user.setLastName(clientDTO.getLastName());
        user.setEmail(clientDTO.getEmail());
        user.setPhone(clientDTO.getPhone());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Get assigned staff if provided
        Staff assignedStaff = null;
        if (clientDTO.getAssignedStaffId() != null) {
            assignedStaff = staffRepository.findById(clientDTO.getAssignedStaffId())
                    .orElseThrow(() -> new RuntimeException("Staff not found"));
        }

        // Get current authenticated user for updatedBy
        User currentUser = getCurrentUser();

        client.setCompanyName(clientDTO.getCompanyName());
        client.setCompanyType(clientDTO.getCompanyType());
        client.setGstNumber(clientDTO.getGstNumber());
        client.setPanNumber(clientDTO.getPanNumber());
        client.setAddress(clientDTO.getAddress());
        client.setCity(clientDTO.getCity());
        client.setState(clientDTO.getState());
        client.setPincode(clientDTO.getPincode());
        client.setCountry(clientDTO.getCountry());
        client.setBusinessType(clientDTO.getBusinessType());
        client.setIndustry(clientDTO.getIndustry());
        client.setWebsite(clientDTO.getWebsite());
        client.setContactPerson(clientDTO.getContactPerson());
        client.setContactPhone(clientDTO.getContactPhone());
        client.setContactEmail(clientDTO.getContactEmail());
        client.setEmergencyContact(clientDTO.getEmergencyContact());
        client.setClientType(clientDTO.getClientType());
        client.setRegistrationDate(clientDTO.getRegistrationDate());
        client.setAssignedStaff(assignedStaff);
        client.setUpdatedAt(LocalDateTime.now());
        client.setUpdatedBy(currentUser);

        Client updatedClient = clientRepository.save(client);
        return convertToDTO(updatedClient);
    }

    @Override
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Get the user associated with this client
        User user = client.getUser();

        // First delete the client record
        clientRepository.delete(client);

        // Then delete the associated user
        userRepository.delete(user);
    }

    @Override
    public void assignStaffToClient(Long clientId, Long staffId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Get current authenticated user for updatedBy
        User currentUser = getCurrentUser();

        client.setAssignedStaff(staff);
        client.setUpdatedAt(LocalDateTime.now());
        client.setUpdatedBy(currentUser);
        clientRepository.save(client);

        // Send notification for staff assignment
        notificationService.notifyStaffAssignedToClient(clientId, staffId);
    }

    @Override
    public Page<Object[]> getClientDocuments(Long clientId, Pageable pageable) {
        // Check current user's role first
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Page.empty(pageable);
        }

        // If user has ADMIN role, allow access to all clients
        if (currentUser.getRole() == User.Role.ADMIN) {
            return clientRepository.findClientDocuments(clientId, pageable);
        }

        // STAFF role - check if client is assigned to them
        Staff currentStaff = getCurrentStaff();
        if (currentStaff == null) {
            return Page.empty(pageable);
        }

        // Get client IDs from tasks assigned to this staff
        List<Long> taskBasedClientIds = taskRepository.getUniqueClientIdsByStaff(currentStaff.getId());

        // Get client IDs directly assigned to this staff in the client table
        List<Long> directlyAssignedClientIds = clientRepository.findClientIdsByAssignedStaff(currentStaff.getId());

        // Combine both lists and remove duplicates
        List<Long> allAssignedClientIds = new ArrayList<>();
        allAssignedClientIds.addAll(taskBasedClientIds);
        allAssignedClientIds.addAll(directlyAssignedClientIds);

        // Remove duplicates while preserving order
        List<Long> uniqueAssignedClientIds = allAssignedClientIds.stream()
                .distinct()
                .collect(Collectors.toList());

        if (!uniqueAssignedClientIds.contains(clientId)) {
            // Client not assigned to this staff member
            return Page.empty(pageable);
        }

        return clientRepository.findClientDocuments(clientId, pageable);
    }

    @Override
    public Page<ClientActivityDTO> getClientActivity(Long clientId, Pageable pageable) {
        // For now, return empty page. Activity tracking can be implemented later
        return Page.empty(pageable);
    }

    @Override
    public Page<Object[]> getClientTasks(Long clientId, Pageable pageable) {
        // Check current user's role first
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Page.empty(pageable);
        }

        // If user has ADMIN role, allow access to all clients
        if (currentUser.getRole() == User.Role.ADMIN) {
            return clientRepository.findClientTasks(clientId, pageable);
        }

        // STAFF role - check if client is assigned to them
        Staff currentStaff = getCurrentStaff();
        if (currentStaff == null) {
            return Page.empty(pageable);
        }

        // Get client IDs from tasks assigned to this staff
        List<Long> taskBasedClientIds = taskRepository.getUniqueClientIdsByStaff(currentStaff.getId());

        // Get client IDs directly assigned to this staff in the client table
        List<Long> directlyAssignedClientIds = clientRepository.findClientIdsByAssignedStaff(currentStaff.getId());

        // Combine both lists and remove duplicates
        List<Long> allAssignedClientIds = new ArrayList<>();
        allAssignedClientIds.addAll(taskBasedClientIds);
        allAssignedClientIds.addAll(directlyAssignedClientIds);

        // Remove duplicates while preserving order
        List<Long> uniqueAssignedClientIds = allAssignedClientIds.stream()
                .distinct()
                .collect(Collectors.toList());

        if (!uniqueAssignedClientIds.contains(clientId)) {
            // Client not assigned to this staff member
            return Page.empty(pageable);
        }

        return clientRepository.findClientTasks(clientId, pageable);
    }

    @Override
    public Map<String, Object> getClientStats() {
        Object[] stats = clientRepository.getClientStats();
        Map<String, Object> result = new HashMap<>();
        result.put("totalClients", stats[0]);
        result.put("activeClients", stats[1]);
        result.put("inactiveClients", stats[2]);
        result.put("clientsWithAssignedStaff", stats[3]);
        return result;
    }

    @Override
    public void toggleClientStatus(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Get current authenticated user for updatedBy
        User currentUser = getCurrentUser();

        // Toggle the active status
        client.setIsActive(!client.getIsActive());
        client.setUpdatedAt(LocalDateTime.now());
        client.setUpdatedBy(currentUser);

        // Also update the user's isActive status to match the client status
        if (client.getUser() != null) {
            User user = client.getUser();
            user.setIsActive(client.getIsActive());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        clientRepository.save(client);
    }

    private ClientDTO convertToDTO(Client client) {
        ClientDTO.ClientDTOBuilder builder = ClientDTO.builder()
                .id(client.getId())
                .userId(client.getUser().getId())
                .email(client.getUser().getEmail())
                .firstName(client.getUser().getFirstName())
                .lastName(client.getUser().getLastName())
                .phone(client.getUser().getPhone())
                .companyName(client.getCompanyName())
                .companyType(client.getCompanyType())
                .gstNumber(client.getGstNumber())
                .panNumber(client.getPanNumber())
                .address(client.getAddress())
                .city(client.getCity())
                .state(client.getState())
                .pincode(client.getPincode())
                .country(client.getCountry())
                .businessType(client.getBusinessType())
                .industry(client.getIndustry())
                .website(client.getWebsite())
                .contactPerson(client.getContactPerson())
                .contactPhone(client.getContactPhone())
                .contactEmail(client.getContactEmail())
                .emergencyContact(client.getEmergencyContact())
                .clientType(client.getClientType())
                .registrationDate(client.getRegistrationDate())
                .isActive(client.getIsActive())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt());

        if (client.getAssignedStaff() != null) {
            builder.assignedStaffId(client.getAssignedStaff().getId());
            if (client.getAssignedStaff().getUser() != null) {
                builder.assignedStaffName(client.getAssignedStaff().getUser().getFirstName() + " "
                        + client.getAssignedStaff().getUser().getLastName());
            }
        }
        return builder.build();
    }

    private ClientDetailDTO convertToDetailDTO(Client client) {
        ClientDetailDTO dto = ClientDetailDTO.builder()
                .id(client.getId())
                .userId(client.getUser().getId())
                .email(client.getUser().getEmail())
                .firstName(client.getUser().getFirstName())
                .lastName(client.getUser().getLastName())
                .phone(client.getUser().getPhone())
                .companyName(client.getCompanyName())
                .companyType(client.getCompanyType())
                .gstNumber(client.getGstNumber())
                .panNumber(client.getPanNumber())
                .address(client.getAddress())
                .city(client.getCity())
                .state(client.getState())
                .pincode(client.getPincode())
                .country(client.getCountry())
                .businessType(client.getBusinessType())
                .industry(client.getIndustry())
                .website(client.getWebsite())
                .contactPerson(client.getContactPerson())
                .contactPhone(client.getContactPhone())
                .contactEmail(client.getContactEmail())
                .emergencyContact(client.getEmergencyContact())
                .clientType(client.getClientType())
                .registrationDate(client.getRegistrationDate())
                .isActive(client.getIsActive())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();

        if (client.getAssignedStaff() != null) {
            dto.setAssignedStaffId(client.getAssignedStaff().getId());
            User staffUser = client.getAssignedStaff().getUser();
            dto.setAssignedStaffName(staffUser.getFirstName() + " " + staffUser.getLastName());
            dto.setAssignedStaffEmail(staffUser.getEmail());
            dto.setAssignedStaffPhone(staffUser.getPhone());
        }

        // Add created by information
        if (client.getCreatedBy() != null) {
            dto.setCreatedBy(client.getCreatedBy().getFirstName() + " " + client.getCreatedBy().getLastName());
            dto.setCreatedById(client.getCreatedBy().getId());
        }

        // Add updated by information
        if (client.getUpdatedBy() != null) {
            dto.setUpdatedBy(client.getUpdatedBy().getFirstName() + " " + client.getUpdatedBy().getLastName());
            dto.setUpdatedById(client.getUpdatedBy().getId());
        }

        return dto;
    }

    @Override
    public ClientDashboardStatsDTO getClientDashboardStats(Long clientId) {
        // Get task statistics
        Long totalTasks = clientRepository.countTasksByClientId(clientId);
        Long pendingTasks = clientRepository.countTasksByClientIdAndStatus(clientId, "PENDING");
        Long completedTasks = clientRepository.countTasksByClientIdAndStatus(clientId, "COMPLETED");
        Long overdueTasks = clientRepository.countOverdueTasksByClientId(clientId);

        // Get document statistics
        Long pendingDocuments = clientRepository.countDocumentsByClientIdAndStatus(clientId, "PENDING");
        Long verifiedDocuments = clientRepository.countDocumentsByClientIdAndStatus(clientId, "VERIFIED");

        // Get upcoming deadlines (tasks due within 7 days)
        Long upcomingDeadlines = clientRepository.countUpcomingDeadlinesByClientId(clientId);

        return ClientDashboardStatsDTO.builder()
                .totalTasks(totalTasks)
                .pendingTasks(pendingTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .pendingDocuments(pendingDocuments)
                .verifiedDocuments(verifiedDocuments)
                .upcomingDeadlines(upcomingDeadlines)
                .build();
    }

    @Override
    public List<ClientActivityDTO> getRecentClientActivities(Long clientId, int limit) {
        return auditLogService.getRecentClientActivities(clientId, limit);
    }

    @Override
    public Long getClientIdByUserId(Long userId) {
        return clientRepository.findClientIdByUserId(userId);
    }

    @Override
    public ClientDTO updateClientProfile(Long clientId, Map<String, Object> profileUpdates) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Update user information - only if provided
        User user = client.getUser();
        boolean userUpdated = false;

        if (profileUpdates.containsKey("firstName")) {
            user.setFirstName((String) profileUpdates.get("firstName"));
            userUpdated = true;
        }
        if (profileUpdates.containsKey("lastName")) {
            user.setLastName((String) profileUpdates.get("lastName"));
            userUpdated = true;
        }
        if (profileUpdates.containsKey("phone")) {
            user.setPhone((String) profileUpdates.get("phone"));
            userUpdated = true;
        }

        if (userUpdated) {
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // Get current authenticated user for updatedBy
        User currentUser = getCurrentUser();

        // Update client information - only if provided
        if (profileUpdates.containsKey("address")) {
            client.setAddress((String) profileUpdates.get("address"));
        }
        if (profileUpdates.containsKey("city")) {
            client.setCity((String) profileUpdates.get("city"));
        }
        if (profileUpdates.containsKey("state")) {
            client.setState((String) profileUpdates.get("state"));
        }
        if (profileUpdates.containsKey("postalCode")) {
            client.setPincode((String) profileUpdates.get("postalCode"));
        }
        if (profileUpdates.containsKey("country")) {
            client.setCountry((String) profileUpdates.get("country"));
        }

        client.setUpdatedAt(LocalDateTime.now());
        client.setUpdatedBy(currentUser);

        Client updatedClient = clientRepository.save(client);
        return convertToDTO(updatedClient);
    }
}