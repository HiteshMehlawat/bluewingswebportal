package com.adspeek.authservice.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStatsDTO {
    private int totalClients;
    private int activeCases;
    private int pendingTasks;
    private int completedFilings;
    private int totalStaff;
    private List<StaffPerformance> staffPerformance;
    private List<LatestUpload> latestUploads;
    private List<UpcomingDeadline> upcomingDeadlines;
    private Map<String, Object> leadStatistics;
    private List<RecentLead> recentLeads;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StaffPerformance {
        private String name;
        private int completed;
        private int pending;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LatestUpload {
        private String client;
        private String file;
        private String date;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpcomingDeadline {
        private Long taskId;
        private String taskName;
        private String description;
        private String dueDate;
        private String status;
        private String priority;
        private String taskType;
        private Long staffId;
        private String staffName;
        private String latestRemark;
        private String deadlineStatus;
        private Boolean hasDocuments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChartPoint {
        private String label;
        private int count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentLead {
        private Long id;
        private String leadId;
        private String name;
        private String email;
        private String phone;
        private String status;
        private String priority;
        private String serviceRequired;
        private String createdAt;
    }
}