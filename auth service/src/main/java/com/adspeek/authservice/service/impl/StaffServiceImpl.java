package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.StaffDTO;
import com.adspeek.authservice.dto.StaffPerformanceSummaryDTO;
import com.adspeek.authservice.entity.Staff;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return null;
    }

    private StaffDTO toDTO(Staff staff) {
        if (staff == null)
            return null;

        StaffDTO.StaffDTOBuilder builder = StaffDTO.builder()
                .id(staff.getId())
                .userId(staff.getUser() != null ? staff.getUser().getId() : null)
                .employeeId(staff.getEmployeeId())
                .position(staff.getPosition())
                .department(staff.getDepartment())
                .joiningDate(staff.getJoiningDate())
                .salary(staff.getSalary())
                .supervisorId(staff.getSupervisor() != null ? staff.getSupervisor().getId() : null)
                .isAvailable(staff.getIsAvailable())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .firstName(staff.getUser() != null ? staff.getUser().getFirstName() : null)
                .lastName(staff.getUser() != null ? staff.getUser().getLastName() : null)
                .email(staff.getUser() != null ? staff.getUser().getEmail() : null)
                .phone(staff.getUser() != null ? staff.getUser().getPhone() : null)
                .role(staff.getUser() != null ? staff.getUser().getRole().name() : null)
                .createdBy(staff.getCreatedBy() != null
                        ? staff.getCreatedBy().getFirstName() + " " + staff.getCreatedBy().getLastName()
                        : null)
                .createdById(staff.getCreatedBy() != null ? staff.getCreatedBy().getId() : null)
                .updatedBy(staff.getUpdatedBy() != null
                        ? staff.getUpdatedBy().getFirstName() + " " + staff.getUpdatedBy().getLastName()
                        : null)
                .updatedById(staff.getUpdatedBy() != null ? staff.getUpdatedBy().getId() : null);

        // Add supervisor details if available
        if (staff.getSupervisor() != null) {
            builder.supervisorName(
                    staff.getSupervisor().getUser() != null ? staff.getSupervisor().getUser().getFirstName() + " "
                            + staff.getSupervisor().getUser().getLastName() : null);
            builder.supervisorEmail(
                    staff.getSupervisor().getUser() != null ? staff.getSupervisor().getUser().getEmail() : null);
            builder.supervisorPhone(
                    staff.getSupervisor().getUser() != null ? staff.getSupervisor().getUser().getPhone() : null);
            builder.supervisorEmployeeId(staff.getSupervisor().getEmployeeId());
        }

        return builder.build();
    }

    private Staff toEntity(StaffDTO dto) {
        if (dto == null)
            return null;
        User user = dto.getUserId() != null ? userRepository.findById(dto.getUserId()).orElse(null) : null;
        Staff supervisor = dto.getSupervisorId() != null ? staffRepository.findById(dto.getSupervisorId()).orElse(null)
                : null;
        return Staff.builder()
                .id(dto.getId())
                .user(user)
                .employeeId(dto.getEmployeeId())
                .position(dto.getPosition())
                .department(dto.getDepartment())
                .joiningDate(dto.getJoiningDate())
                .salary(dto.getSalary())
                .supervisor(supervisor)
                .isAvailable(dto.getIsAvailable())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) {
        User currentUser = getCurrentUser();
        User user;

        if (staffDTO.getUserId() != null) {
            // Use existing user
            user = userRepository.findById(staffDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            // Create new user for the staff
            user = User.builder()
                    .firstName(staffDTO.getFirstName())
                    .lastName(staffDTO.getLastName())
                    .email(staffDTO.getEmail())
                    .phone(staffDTO.getPhone())
                    .role(staffDTO.getRole() != null ? User.Role.valueOf(staffDTO.getRole()) : User.Role.STAFF)
                    .isActive(staffDTO.getIsAvailable() != null ? staffDTO.getIsAvailable() : true)
                    .emailVerified(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Set a default password (staff will change it later)
            user.setPasswordHash("$2a$10$U1EgKPoUls0Bb4GP0JyCWe4IvlBayph/d83SLZ0Lh4wxxJAQUheD2"); // "admin"
            user = userRepository.save(user);
        }

        // Create staff with the user
        Staff staff = Staff.builder()
                .user(user)
                .employeeId(staffDTO.getEmployeeId())
                .position(staffDTO.getPosition())
                .department(staffDTO.getDepartment())
                .joiningDate(staffDTO.getJoiningDate())
                .salary(staffDTO.getSalary())
                .isAvailable(staffDTO.getIsAvailable() != null ? staffDTO.getIsAvailable() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        // Set supervisor if provided
        if (staffDTO.getSupervisorId() != null) {
            Staff supervisor = staffRepository.findById(staffDTO.getSupervisorId()).orElse(null);
            staff.setSupervisor(supervisor);
        }

        return toDTO(staffRepository.save(staff));
    }

    @Override
    public StaffDTO updateStaff(Long id, StaffDTO staffDTO) {
        Optional<Staff> staffOpt = staffRepository.findById(id);
        if (staffOpt.isEmpty())
            return null;
        Staff staff = staffOpt.get();
        User currentUser = getCurrentUser();

        // Update user information if staff has a user
        if (staff.getUser() != null) {
            User user = staff.getUser();
            user.setFirstName(staffDTO.getFirstName());
            user.setLastName(staffDTO.getLastName());
            user.setEmail(staffDTO.getEmail());
            user.setPhone(staffDTO.getPhone());
            user.setIsActive(staffDTO.getIsAvailable());
            // Update role if provided
            if (staffDTO.getRole() != null) {
                user.setRole(User.Role.valueOf(staffDTO.getRole()));
            }
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // Update staff fields
        staff.setPosition(staffDTO.getPosition());
        staff.setDepartment(staffDTO.getDepartment());
        staff.setJoiningDate(staffDTO.getJoiningDate());
        staff.setSalary(staffDTO.getSalary());
        staff.setIsAvailable(staffDTO.getIsAvailable());
        staff.setUpdatedBy(currentUser);
        staff.setUpdatedAt(LocalDateTime.now());

        if (staffDTO.getSupervisorId() != null) {
            staff.setSupervisor(staffRepository.findById(staffDTO.getSupervisorId()).orElse(null));
        }
        return toDTO(staffRepository.save(staff));
    }

    @Override
    public void deleteStaff(Long id) {
        // staffRepository.deleteById(id);

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Get the user associated with this Staff
        User user = staff.getUser();

        // First delete the Staff record
        staffRepository.delete(staff);

        // Then delete the associated user
        userRepository.delete(user);
    }

    @Override
    public Optional<StaffDTO> getStaffById(Long id) {
        return staffRepository.findByIdWithSupervisor(id).map(this::toDTO);
    }

    @Override
    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Page<StaffDTO> getStaffPage(Pageable pageable) {
        return staffRepository.findAllWithUser(pageable).map(this::toDTO);
    }

    @Override
    public Page<StaffDTO> getStaffPageWithFilters(Pageable pageable, String search, String department, String status) {
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasDepartment = department != null && !department.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();

        Page<Staff> staffPage;

        if (hasSearch && hasDepartment && hasStatus) {
            // All filters
            Boolean isAvailable = Boolean.parseBoolean(status);
            staffPage = staffRepository.findBySearchTermAndDepartmentAndStatus(search.trim(), department.trim(),
                    isAvailable, pageable);
        } else if (hasSearch && hasDepartment) {
            // Search + Department
            staffPage = staffRepository.findBySearchTermAndDepartment(search.trim(), department.trim(), pageable);
        } else if (hasSearch && hasStatus) {
            // Search + Status
            Boolean isAvailable = Boolean.parseBoolean(status);
            staffPage = staffRepository.findBySearchTermAndStatus(search.trim(), isAvailable, pageable);
        } else if (hasDepartment && hasStatus) {
            // Department + Status
            Boolean isAvailable = Boolean.parseBoolean(status);
            staffPage = staffRepository.findByDepartmentAndStatus(department.trim(), isAvailable, pageable);
        } else if (hasSearch) {
            // Only search
            staffPage = staffRepository.findBySearchTerm(search.trim(), pageable);
        } else if (hasDepartment) {
            // Only department
            staffPage = staffRepository.findByDepartment(department.trim(), pageable);
        } else if (hasStatus) {
            // Only status
            Boolean isAvailable = Boolean.parseBoolean(status);
            staffPage = staffRepository.findByStatus(isAvailable, pageable);
        } else {
            // No filters
            staffPage = staffRepository.findAllWithUser(pageable);
        }

        return staffPage.map(this::toDTO);
    }

    @Override
    public List<String> getAllDepartments() {
        return staffRepository.findAllDepartments();
    }

    @Override
    public Page<StaffPerformanceSummaryDTO> getStaffPerformanceSummary(Pageable pageable) {
        Page<Object[]> page = staffRepository.findStaffPerformanceSummaryNative(pageable);
        return page.map(row -> {
            LocalDateTime lastActivity = null;
            if (row[9] != null) {
                long epoch = ((Number) row[9]).longValue();
                if (epoch > 0) {
                    lastActivity = LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC);
                }
            }
            return new StaffPerformanceSummaryDTO(
                    (String) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    row[4] != null ? ((Number) row[4]).longValue() : null,
                    row[5] != null ? ((Number) row[5]).longValue() : 0L,
                    row[6] != null ? ((Number) row[6]).longValue() : 0L,
                    row[7] != null ? ((Number) row[7]).longValue() : 0L,
                    row[8] != null ? ((Number) row[8]).longValue() : 0L,
                    0L, // totalAssignedClients - not available in summary query
                    lastActivity);
        });
    }

    @Override
    public Optional<StaffPerformanceSummaryDTO> getStaffPerformanceById(Long staffId) {
        try {
            // Use the main performance query
            List<Object[]> rows = staffRepository.findStaffPerformanceByIdNative(staffId);

            if (rows.isEmpty()) {
                return Optional.empty();
            }

            Object[] row = rows.get(0);

            // Safely extract values from the Object array
            String employeeId = row.length > 0 ? String.valueOf(row[0]) : null;
            String name = row.length > 1 ? String.valueOf(row[1]) : null;
            String position = row.length > 2 ? String.valueOf(row[2]) : null;
            String department = row.length > 3 ? String.valueOf(row[3]) : null;

            Long supervisorId = null;
            if (row.length > 4 && row[4] != null) {
                try {
                    supervisorId = ((Number) row[4]).longValue();
                } catch (Exception e) {
                    supervisorId = null;
                }
            }

            Long totalAssigned = 0L;
            if (row.length > 5 && row[5] != null) {
                try {
                    totalAssigned = ((Number) row[5]).longValue();
                } catch (Exception e) {
                    totalAssigned = 0L;
                }
            }

            Long completed = 0L;
            if (row.length > 6 && row[6] != null) {
                try {
                    completed = ((Number) row[6]).longValue();
                } catch (Exception e) {
                    completed = 0L;
                }
            }

            Long pending = 0L;
            if (row.length > 7 && row[7] != null) {
                try {
                    pending = ((Number) row[7]).longValue();
                } catch (Exception e) {
                    pending = 0L;
                }
            }

            Long overdue = 0L;
            if (row.length > 8 && row[8] != null) {
                try {
                    overdue = ((Number) row[8]).longValue();
                } catch (Exception e) {
                    overdue = 0L;
                }
            }

            Long totalAssignedClients = 0L;
            if (row.length > 9 && row[9] != null) {
                try {
                    totalAssignedClients = ((Number) row[9]).longValue();
                } catch (Exception e) {
                    totalAssignedClients = 0L;
                }
            }

            LocalDateTime lastActivity = null;
            if (row.length > 10 && row[10] != null) {
                try {
                    long epoch = ((Number) row[10]).longValue();
                    if (epoch > 0) {
                        lastActivity = LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC);
                    }
                } catch (Exception e) {
                    // If epoch conversion fails, set to null
                    lastActivity = null;
                }
            }

            StaffPerformanceSummaryDTO dto = new StaffPerformanceSummaryDTO(
                    employeeId, name, position, department, supervisorId,
                    totalAssigned, completed, pending, overdue, totalAssignedClients, lastActivity);

            return Optional.of(dto);
        } catch (Exception e) {
            // Log the error and return empty
            System.err.println("Error fetching staff performance for ID " + staffId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public StaffDTO getCurrentStaff() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }

        // Find staff by user ID
        Optional<Staff> staff = staffRepository.findAll().stream()
                .filter(s -> s.getUser() != null && s.getUser().getId().equals(currentUser.getId()))
                .findFirst();

        return staff.map(this::toDTO).orElse(null);
    }

    @Override
    public StaffDTO updateMyProfile(StaffDTO staffDTO) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }

        // Find staff by user ID
        Optional<Staff> staffOpt = staffRepository.findAll().stream()
                .filter(s -> s.getUser() != null && s.getUser().getId().equals(currentUser.getId()))
                .findFirst();

        if (staffOpt.isEmpty()) {
            return null;
        }

        Staff staff = staffOpt.get();

        // Only allow updating certain fields for security
        if (staffDTO.getFirstName() != null) {
            staff.getUser().setFirstName(staffDTO.getFirstName());
        }
        if (staffDTO.getLastName() != null) {
            staff.getUser().setLastName(staffDTO.getLastName());
        }
        if (staffDTO.getPhone() != null) {
            staff.getUser().setPhone(staffDTO.getPhone());
        }

        // Save the updated user
        User savedUser = userRepository.save(staff.getUser());
        staff.setUser(savedUser);

        // Save the updated staff
        Staff savedStaff = staffRepository.save(staff);

        return toDTO(savedStaff);
    }
}