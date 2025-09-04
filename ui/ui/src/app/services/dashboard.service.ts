import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

export interface StaffPerformance {
  name: string;
  completed: number;
  pending: number;
}
export interface LatestUpload {
  client: string;
  file: string;
  date: string;
}
export interface UpcomingDeadline {
  taskId: number;
  taskName: string;
  description: string;
  dueDate: string;
  status: string;
  priority: string;
  taskType: string;
  staffId: number;
  staffName: string;
  latestRemark: string;
  deadlineStatus: string;
  hasDocuments: boolean;
}
export interface ChartPoint {
  label: string;
  count: number;
}
export interface DashboardStats {
  totalClients: number;
  activeCases: number;
  pendingTasks: number;
  completedFilings: number;
  totalStaff: number;
  staffPerformance: StaffPerformance[];
  latestUploads: LatestUpload[];
  upcomingDeadlines: UpcomingDeadline[];
  chartData: { weekly: ChartPoint[]; monthly: ChartPoint[] };
}

export interface LatestDocumentUpload {
  documentId: number;
  documentName: string;
  originalFileName: string;
  uploadDateTime: string;
  fileType: string;
  fileSize: number;
  documentType: string;
  isVerified: boolean;
  status: string | null;
  uploadedById: number;
  uploadedByName: string;
  uploadedByRole: string;
  clientId: number;
  clientName: string;
  taskName: string | null;
  verifiedById: number | null;
  verifiedByName: string | null;
  verifiedAt: string | null;
  rejectedById: number | null;
  rejectedByName: string | null;
  rejectedAt: string | null;
  rejectionReason: string | null;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private adminDashboardUrl = API_CONFIG.baseUrl + API_CONFIG.adminDashboard;
  private staffPerformanceUrl =
    API_CONFIG.baseUrl + API_CONFIG.staffPerformance;
  private documentsUrl = API_CONFIG.baseUrl + API_CONFIG.documents;
  private tasksUrl = API_CONFIG.baseUrl + API_CONFIG.tasks;

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(this.adminDashboardUrl);
  }
  getStaffPerformanceSummary(page: number, size: number) {
    return this.http.get<{
      content: any[];
      totalElements: number;
      totalPages: number;
    }>(`${this.staffPerformanceUrl}?page=${page}&size=${size}`);
  }
  getLatestDocumentUploads(page: number, size: number) {
    return this.http.get<{
      content: LatestDocumentUpload[];
      totalElements: number;
      totalPages: number;
    }>(`${this.documentsUrl}/latest?page=${page}&size=${size}`);
  }
  getUpcomingDeadlines(clientId: number, page: number, size: number) {
    return this.http.get<{
      content: UpcomingDeadline[];
      totalElements: number;
      totalPages: number;
    }>(
      `${this.tasksUrl}/upcoming-deadlines?clientId=${clientId}&page=${page}&size=${size}`
    );
  }

  getAdminUpcomingDeadlines(page: number, size: number) {
    return this.http.get<{
      content: any[];
      totalElements: number;
      totalPages: number;
    }>(
      `${API_CONFIG.baseUrl}${API_CONFIG.admin}/upcoming-deadlines?page=${page}&size=${size}`
    );
  }

  getCompletedFilingsCount() {
    return this.http.get<number>(
      `${API_CONFIG.baseUrl}${API_CONFIG.admin}/completed-filings-count`
    );
  }
}
