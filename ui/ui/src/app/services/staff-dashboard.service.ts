import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

export interface StaffDashboardStats {
  totalAssignedTasks: number;
  pendingTasks: number;
  inProgressTasks: number;
  completedTasks: number;
  onHoldTasks: number;
  cancelledTasks: number;
  overdueTasks: number;
  totalAssignedClients: number;
  averageTaskCompletionTime: number;
}

export interface StaffTask {
  id: number;
  title: string;
  description: string;
  clientName: string;
  clientEmail: string;
  clientPhone: string;
  taskType: string;
  status: string;
  priority: string;
  dueDate: string;
  assignedDate: string;
  startedDate?: string;
  completedDate?: string;
  estimatedHours?: number;
  actualHours?: number;
  deadlineStatus: string;
  hasDocuments: boolean;
}

export interface StaffActivity {
  id: number;
  activityType: string;
  title: string;
  description: string;
  taskTitle?: string;
  clientName?: string;
  workStatus?: string;
  timestamp: string;
  icon: string;
  color: string;
}

@Injectable({
  providedIn: 'root',
})
export class StaffDashboardService {
  private baseUrl = API_CONFIG.baseUrl;

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<StaffDashboardStats> {
    return this.http.get<StaffDashboardStats>(
      `${this.baseUrl}/api/staff-dashboard/stats`
    );
  }

  getRecentTasks(page: number = 0, size: number = 3): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<{
      content: any[];
      totalElements: number;
      totalPages: number;
    }>(`${this.baseUrl}/api/staff-dashboard/tasks`, {
      params,
    });
  }

  getRecentActivities(page: number = 0, size: number = 5): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get(`${this.baseUrl}/api/staff-dashboard/activities`, {
      params,
    });
  }

  // New methods for staff to view their own attendance and performance
  getMyAttendance(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get(`${this.baseUrl}/api/staff-activities/my-attendance`, {
      params,
    });
  }

  getMyAttendanceByDateRange(
    startDate: string,
    endDate: string,
    page: number = 0,
    size: number = 10
  ): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get(
      `${this.baseUrl}/api/staff-activities/my-attendance/date-range`,
      {
        params,
      }
    );
  }

  getMyPerformance(startDate: string, endDate: string): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get(
      `${this.baseUrl}/api/staff-activities/my-performance`,
      {
        params,
      }
    );
  }

  getMyDailySummary(date: string): Observable<any> {
    const params = new HttpParams().set('date', date);
    return this.http.get(
      `${this.baseUrl}/api/staff-activities/my-daily-summary`,
      {
        params,
      }
    );
  }

  getMyWeeklySummary(startDate: string, endDate: string): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get(
      `${this.baseUrl}/api/staff-activities/my-weekly-summary`,
      {
        params,
      }
    );
  }
}
