package com.adspeek.authservice.service;

import com.adspeek.authservice.dto.LeadDTO;
import com.adspeek.authservice.dto.PublicLeadRequest;
import com.adspeek.authservice.entity.Lead;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LeadService {

        LeadDTO createLead(LeadDTO leadDTO);

        LeadDTO createPublicLead(PublicLeadRequest request);

        LeadDTO updateLead(Long id, LeadDTO leadDTO);

        // Staff-specific update method that only allows editing certain fields
        LeadDTO updateLeadByStaff(Long id, LeadDTO leadDTO);

        LeadDTO getLeadById(Long id);

        LeadDTO getLeadByLeadId(String leadId);

        Page<LeadDTO> getAllLeads(int page, int size);

        // Service hierarchy filter methods
        Page<LeadDTO> getLeadsWithServiceHierarchyFilters(int page, int size, String search, Lead.Status status,
                        Lead.Priority priority, Long serviceCategoryId, Long serviceSubcategoryId, Long serviceItemId);

        // Staff-specific service hierarchy filter method
        Page<LeadDTO> getLeadsByAssignedStaffWithServiceHierarchyFilters(Long staffId, int page, int size,
                        String search,
                        Lead.Status status, Lead.Priority priority, Long serviceCategoryId, Long serviceSubcategoryId,
                        Long serviceItemId);

        LeadDTO assignLeadToStaff(Long leadId, Long staffId);

        LeadDTO updateLeadStatus(Long leadId, Lead.Status status, String reason);

        LeadDTO convertLeadToClient(Long leadId);

        void deleteLead(Long id);

        List<LeadDTO> getLeadsByFollowUpDate(LocalDate date);

        List<LeadDTO> getOverdueFollowUps();

        Map<String, Object> getLeadStatistics();

        Map<String, Object> getLeadStatisticsByStaff(Long staffId);

        Page<LeadDTO> getRecentLeads(int page, int size);
}
