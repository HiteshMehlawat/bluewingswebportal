package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.StaffDTO;
import com.adspeek.authservice.dto.StaffPerformanceSummaryDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StaffService {
    StaffDTO createStaff(StaffDTO staffDTO);

    StaffDTO updateStaff(Long id, StaffDTO staffDTO);

    void deleteStaff(Long id);

    Optional<StaffDTO> getStaffById(Long id);

    List<StaffDTO> getAllStaff();

    Page<StaffDTO> getStaffPage(Pageable pageable);

    Page<StaffDTO> getStaffPageWithFilters(Pageable pageable, String search, String department, String status);

    List<String> getAllDepartments();

    Page<StaffPerformanceSummaryDTO> getStaffPerformanceSummary(Pageable pageable);

    Optional<StaffPerformanceSummaryDTO> getStaffPerformanceById(Long staffId);

    StaffDTO getCurrentStaff();

    StaffDTO updateMyProfile(StaffDTO staffDTO);
}