package com.adspeek.authservice.service.impl;

import com.adspeek.authservice.dto.AdminDashboardStatsDTO;
import com.adspeek.authservice.dto.AdminDashboardStatsDTO.ChartPoint;
import com.adspeek.authservice.repository.AdminDashboardRepository;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.service.AdminDashboardService;
import com.adspeek.authservice.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final AdminDashboardRepository adminDashboardRepository;
    private final StaffRepository staffRepository;
    private final LeadService leadService;

    @Override
    public AdminDashboardStatsDTO getDashboardStats() {
        // 1. Summary
        Map<String, Object> summary = adminDashboardRepository.getSummary();
        int totalClients = ((Number) summary.getOrDefault("total_clients", 0)).intValue();
        int activeCases = ((Number) summary.getOrDefault("active_cases", 0)).intValue();
        int pendingTasks = ((Number) summary.getOrDefault("pending_tasks", 0)).intValue();
        int completedFilings = adminDashboardRepository.getCompletedFilingsCount().intValue();
        int totalStaff = ((Number) summary.getOrDefault("total_staff", 0)).intValue();

        // 2. Staff performance (for all staff)
        List<AdminDashboardStatsDTO.StaffPerformance> staffPerformance = new ArrayList<>();
        LocalDate start = LocalDate.now().minusMonths(1);
        LocalDate end = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        staffRepository.findAll().forEach(staff -> {
            List<Object[]> perf = adminDashboardRepository.getStaffPerformance(staff.getId(), start.format(fmt),
                    end.format(fmt));
            if (!perf.isEmpty()) {
                Object[] row = perf.get(0);
                staffPerformance.add(AdminDashboardStatsDTO.StaffPerformance.builder()
                        .name(staff.getUser().getFirstName() + " " + staff.getUser().getLastName())
                        .completed(row[1] != null ? ((Number) row[1]).intValue() : 0)
                        .pending(row[2] != null ? ((Number) row[2]).intValue() : 0)
                        .build());
            }
        });

        // 3. Latest uploads
        List<AdminDashboardStatsDTO.LatestUpload> latestUploads = new ArrayList<>();
        for (Object[] row : adminDashboardRepository.getLatestUploads()) {
            latestUploads.add(AdminDashboardStatsDTO.LatestUpload.builder()
                    .client((String) row[0])
                    .file((String) row[1])
                    .date(row[2].toString())
                    .build());
        }

        // 4. Upcoming deadlines (all tasks, not just by client)
        List<AdminDashboardStatsDTO.UpcomingDeadline> upcomingDeadlines = new ArrayList<>();
        for (Object[] row : adminDashboardRepository.getAllUpcomingDeadlines()) {
            upcomingDeadlines.add(AdminDashboardStatsDTO.UpcomingDeadline.builder()
                    .taskId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .taskName((String) row[1])
                    .description((String) row[2])
                    .dueDate(row[3] != null ? row[3].toString() : null)
                    .status((String) row[4])
                    .priority((String) row[5])
                    .taskType((String) row[6])
                    .staffId(row[7] != null ? ((Number) row[7]).longValue() : null)
                    .staffName((String) row[8])
                    .latestRemark((String) row[9])
                    .deadlineStatus((String) row[10])
                    .hasDocuments(row[11] != null && (row[11].toString().equals("1") || row[11].equals(Boolean.TRUE)))
                    .build());
        }

        // 5. Lead statistics
        Map<String, Object> leadStats = leadService.getLeadStatistics();

        // 6. Recent leads (top 3)
        var recentLeadsPage = leadService.getRecentLeads(0, 3);
        List<AdminDashboardStatsDTO.RecentLead> recentLeads = new ArrayList<>();
        for (var lead : recentLeadsPage.getContent()) {
            recentLeads.add(AdminDashboardStatsDTO.RecentLead.builder()
                    .id(lead.getId())
                    .leadId(lead.getLeadId())
                    .name(lead.getFirstName() + " " + lead.getLastName())
                    .email(lead.getEmail())
                    .phone(lead.getPhone())
                    .status(lead.getStatus().toString())
                    .priority(lead.getPriority().toString())
                    .serviceRequired(lead.getServiceItemName() != null ? lead.getServiceItemName() : "Not specified")
                    .createdAt(lead.getCreatedAt().toString())
                    .build());
        }

        return AdminDashboardStatsDTO.builder()
                .totalClients(totalClients)
                .activeCases(activeCases)
                .pendingTasks(pendingTasks)
                .completedFilings(completedFilings)
                .totalStaff(totalStaff)
                .staffPerformance(staffPerformance)
                .latestUploads(latestUploads)
                .upcomingDeadlines(upcomingDeadlines)
                .leadStatistics(leadStats)
                .recentLeads(recentLeads)
                .build();
    }

    @Override
    public Page<AdminDashboardStatsDTO.UpcomingDeadline> getUpcomingDeadlines(Pageable pageable) {
        List<Object[]> allDeadlines = adminDashboardRepository.getAllUpcomingDeadlines();

        // Manual pagination since we're using a native query
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allDeadlines.size());

        List<Object[]> pageContent = allDeadlines.subList(start, end);

        List<AdminDashboardStatsDTO.UpcomingDeadline> deadlines = new ArrayList<>();
        for (Object[] row : pageContent) {
            deadlines.add(AdminDashboardStatsDTO.UpcomingDeadline.builder()
                    .taskId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .taskName((String) row[1])
                    .description((String) row[2])
                    .dueDate(row[3] != null ? row[3].toString() : null)
                    .status((String) row[4])
                    .priority((String) row[5])
                    .taskType((String) row[6])
                    .staffId(row[7] != null ? ((Number) row[7]).longValue() : null)
                    .staffName((String) row[8])
                    .latestRemark((String) row[9])
                    .deadlineStatus((String) row[10])
                    .hasDocuments(row[11] != null && (row[11].toString().equals("1") || row[11].equals(Boolean.TRUE)))
                    .build());
        }

        return new org.springframework.data.domain.PageImpl<>(
                deadlines,
                pageable,
                allDeadlines.size());
    }

    @Override
    public Long getCompletedFilingsCount() {
        return adminDashboardRepository.getCompletedFilingsCount();
    }
}