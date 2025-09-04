import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { API_CONFIG } from '../api.config';
import { AuthService } from '../services/auth.service';

interface StaffActivity {
  id: number;
  staffId: number;
  staffName: string;
  activityType: string;
  taskDescription: string;
  workStatus: string;
  logDate: string;
  loginTime?: string;
  logoutTime?: string;
  durationMinutes?: number;
  clientId?: number;
  clientName?: string;
  taskId?: number;
  taskTitle?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

interface ActivityStats {
  totalActivities: number;
  activeStaff: number;
  averageActivityTime: number;
  mostActiveStaff: string;
}

interface WorkloadSummary {
  staffId: number;
  staffName: string;
  employeeId: string;
  department: string;
  totalActivities: number;
  completedTasks: number;
  pendingTasks: number;
  delayedTasks: number;
  assignedClients: number;
  totalWorkMinutes: number;
  lastActivity: string;
}

interface WorkloadSummaryResponse {
  staffWorkloads: WorkloadSummary[];
  date: string;
  totalStaff: number;
  totalActivities: number;
  averageWorkHours: number;
}

@Component({
  selector: 'app-staff-activities',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './staff-activities.component.html',
  styleUrls: ['./staff-activities.component.scss'],
})
export class StaffActivitiesComponent implements OnInit {
  activities: StaffActivity[] = [];
  workloadSummary: WorkloadSummary[] = [];
  stats: ActivityStats = {
    totalActivities: 0,
    activeStaff: 0,
    averageActivityTime: 0,
    mostActiveStaff: 'N/A',
  };
  loading = false;
  error: string | null = null;
  showWorkloadSummary = false;
  selectedDate = new Date();

  // New properties for attendance and performance
  myActivitiesToday: any[] = [];
  myPerformance: any = null;
  myDailySummary: any = null;
  myWeeklySummary: any[] = [];
  activitiesLoading = false;
  performanceLoading = false;
  currentUser: any = null;
  isStaff = false;

  // Date range for performance metrics
  performanceStartDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
    .toISOString()
    .split('T')[0]; // 30 days ago
  performanceEndDate = new Date().toISOString().split('T')[0]; // Today

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.isStaff = this.authService.isStaff();

    if (this.isStaff) {
      // Load staff-specific data
      this.loadMyActivitiesToday();
      this.loadMyPerformance();
      this.loadMyDailySummary();
      this.loadMyWeeklySummary();
    } else {
      // Load admin view data
      this.loadTodayActivities();
      this.loadStats();
    }
  }

  loadTodayActivities(): void {
    this.loading = true;
    this.showWorkloadSummary = false;
    this.error = null;

    const today = new Date().toISOString().split('T')[0];
    // Load more activities for better scrolling experience
    this.http
      .get<any>(
        `${API_CONFIG.baseUrl}${API_CONFIG['staff-activities']}/date/${today}?page=0&size=20`
      )
      .subscribe({
        next: (response) => {
          // Handle Spring Boot Page response format
          if (response && Array.isArray(response.content)) {
            this.activities = response.content;
          } else if (Array.isArray(response)) {
            this.activities = response;
          } else if (response && Array.isArray(response.activities)) {
            this.activities = response.activities;
          } else {
            this.activities = [];
          }
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Failed to load activities';
          this.loading = false;
          console.error('Error loading activities:', error);
        },
      });
  }

  loadWorkloadSummary(): void {
    this.loading = true;
    this.showWorkloadSummary = true;
    this.error = null;

    const today = new Date().toISOString().split('T')[0];
    this.http
      .get<WorkloadSummaryResponse>(
        `${API_CONFIG.baseUrl}${API_CONFIG['staff-activities']}/workload-summary?date=${today}`
      )
      .subscribe({
        next: (response) => {
          // Handle different response formats
          if (response && Array.isArray(response.staffWorkloads)) {
            this.workloadSummary = response.staffWorkloads;
          } else if (Array.isArray(response)) {
            this.workloadSummary = response;
          } else {
            this.workloadSummary = [];
          }
          this.selectedDate = new Date(response.date || today);
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Failed to load workload summary';
          this.loading = false;
          console.error('Error loading workload summary:', error);
        },
      });
  }

  loadStats(): void {
    this.error = null;

    // Get today's date for stats
    const today = new Date().toISOString().split('T')[0];

    // Load activity stats from backend
    this.http
      .get<any>(
        `${API_CONFIG.baseUrl}${API_CONFIG['staff-activities']}/workload-summary?date=${today}`
      )
      .subscribe({
        next: (response) => {
          if (response) {
            // Calculate total activities from workload summary
            const totalActivities = (response.staffWorkloads || []).reduce(
              (sum: number, workload: any) =>
                sum + (workload.totalActivities || 0),
              0
            );

            // Calculate average activity time from workload summary
            const totalWorkMinutes = (response.staffWorkloads || []).reduce(
              (sum: number, workload: any) =>
                sum + (workload.totalWorkMinutes || 0),
              0
            );
            const averageActivityTime =
              totalActivities > 0
                ? (totalWorkMinutes / totalActivities / 60).toFixed(1)
                : 0;

            this.stats = {
              totalActivities: totalActivities,
              activeStaff: response.totalStaff || 0,
              averageActivityTime: parseFloat(averageActivityTime.toString()),
              mostActiveStaff: this.getMostActiveStaff(
                response.staffWorkloads || []
              ),
            };
          }
        },
        error: (error) => {
          console.error('Error loading stats:', error);
          // Keep default stats if API fails
        },
      });
  }

  // Load activities for a specific date range
  loadActivitiesByDateRange(startDate: string, endDate: string): void {
    this.loading = true;
    this.error = null;

    this.http
      .get<any>(
        `${API_CONFIG.baseUrl}${API_CONFIG['staff-activities']}/date-range?startDate=${startDate}&endDate=${endDate}`
      )
      .subscribe({
        next: (response) => {
          if (response && Array.isArray(response.content)) {
            this.activities = response.content;
          } else if (Array.isArray(response)) {
            this.activities = response;
          } else {
            this.activities = [];
          }
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Failed to load activities for date range';
          this.loading = false;
          console.error('Error loading activities by date range:', error);
        },
      });
  }

  // Load activities for a specific staff member
  loadActivitiesByStaff(staffId: number): void {
    this.loading = true;
    this.error = null;

    this.http
      .get<any>(
        `${API_CONFIG.baseUrl}${API_CONFIG['staff-activities']}/staff/${staffId}`
      )
      .subscribe({
        next: (response) => {
          if (response && Array.isArray(response.content)) {
            this.activities = response.content;
          } else if (Array.isArray(response)) {
            this.activities = response;
          } else {
            this.activities = [];
          }
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Failed to load staff activities';
          this.loading = false;
          console.error('Error loading staff activities:', error);
        },
      });
  }

  private getMostActiveStaff(workloads: WorkloadSummary[]): string {
    if (!workloads || workloads.length === 0) {
      return 'N/A';
    }

    // Find staff with highest total activities
    const mostActive = workloads.reduce((prev, current) =>
      current.totalActivities > prev.totalActivities ? current : prev
    );

    return mostActive.staffName || 'N/A';
  }

  getActivityIcon(activityType: string): string {
    switch (activityType) {
      case 'LOGIN':
        return 'login';
      case 'LOGOUT':
        return 'logout';
      case 'TASK_STARTED':
        return 'play_circle';
      case 'TASK_COMPLETED':
        return 'check_circle';
      case 'TASK_DELAYED':
        return 'schedule';
      case 'CLIENT_ASSIGNED':
        return 'person_add';
      case 'DOCUMENT_UPLOADED':
        return 'upload_file';
      case 'CLIENT_CONTACT':
        return 'phone';
      case 'BREAK_START':
        return 'coffee';
      case 'BREAK_END':
        return 'work';
      default:
        return 'info';
    }
  }

  // Method to refresh data
  refreshData(): void {
    if (this.isStaff) {
      this.loadMyActivitiesToday();
      this.loadMyPerformance();
      this.loadMyDailySummary();
      this.loadMyWeeklySummary();
    } else {
      this.loadTodayActivities();
      this.loadStats();
    }
  }

  // Method to clear error
  clearError(): void {
    this.error = null;
  }

  // New methods for staff attendance and performance
  loadMyActivitiesToday(): void {
    this.activitiesLoading = true;
    const params = new HttpParams().set('page', '0').set('size', '50');

    this.http
      .get(`${API_CONFIG.baseUrl}/api/staff-activities/my-activities-today`, {
        params,
      })
      .subscribe({
        next: (response: any) => {
          this.myActivitiesToday = response.content || [];
          this.activitiesLoading = false;
        },
        error: (error) => {
          console.error("Error loading today's activities:", error);
          this.activitiesLoading = false;
        },
      });
  }

  loadMyPerformance(): void {
    this.performanceLoading = true;
    const params = new HttpParams()
      .set('startDate', this.performanceStartDate)
      .set('endDate', this.performanceEndDate);

    this.http
      .get(`${API_CONFIG.baseUrl}/api/staff-activities/my-performance`, {
        params,
      })
      .subscribe({
        next: (performance) => {
          this.myPerformance = performance;
          this.performanceLoading = false;
        },
        error: (error) => {
          console.error('Error loading performance:', error);
          this.performanceLoading = false;
        },
      });
  }

  loadMyDailySummary(): void {
    const today = new Date().toISOString().split('T')[0];
    const params = new HttpParams().set('date', today);

    this.http
      .get(`${API_CONFIG.baseUrl}/api/staff-activities/my-daily-summary`, {
        params,
      })
      .subscribe({
        next: (summary) => {
          this.myDailySummary = summary;
        },
        error: (error) => {
          console.error('Error loading daily summary:', error);
        },
      });
  }

  loadMyWeeklySummary(): void {
    const endDate = new Date().toISOString().split('T')[0];
    const startDate = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split('T')[0];
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    this.http
      .get(`${API_CONFIG.baseUrl}/api/staff-activities/my-weekly-summary`, {
        params,
      })
      .subscribe({
        next: (summary: any) => {
          this.myWeeklySummary = summary || [];
        },
        error: (error) => {
          console.error('Error loading weekly summary:', error);
        },
      });
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  formatTime(timeString: string): string {
    return new Date(`2000-01-01T${timeString}`).toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
