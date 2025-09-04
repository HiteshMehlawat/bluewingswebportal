import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

export interface Deadline {
  id: number;
  taskId: number;
  taskTitle: string;
  taskDescription: string;
  dueDate: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'OVERDUE';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  taskType: string;
  clientId: number;
  clientName: string;
  assignedStaffId: number;
  assignedStaffName: string;
  daysRemaining: number;
  isOverdue: boolean;
  deadlineStatus: 'SAFE' | 'DUE_SOON' | 'OVERDUE';
  hasDocuments: boolean;
  latestComment?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DeadlineFilters {
  clientId?: number;
  status?: string;
  priority?: string;
  taskType?: string;
  isOverdue?: boolean;
  searchTerm?: string;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
  isFirst: boolean;
  isLast: boolean;
}

export interface DeadlineStatistics {
  totalDeadlines: number;
  pendingDeadlines: number;
  inProgressDeadlines: number;
  completedDeadlines: number;
  overdueDeadlines: number;
  dueSoonDeadlines: number;
  safeDeadlines: number;
}

@Injectable({
  providedIn: 'root',
})
export class DeadlineService {
  private deadlineUrl = API_CONFIG.baseUrl + '/api/deadlines';

  constructor(private http: HttpClient) {}

  // Get all deadlines with pagination and filters
  getDeadlines(
    filters: DeadlineFilters = {}
  ): Observable<PageResponse<Deadline>> {
    const params = new URLSearchParams();

    if (filters.clientId)
      params.append('clientId', filters.clientId.toString());
    if (filters.status) params.append('status', filters.status);
    if (filters.priority) params.append('priority', filters.priority);
    if (filters.taskType) params.append('taskType', filters.taskType);
    if (filters.isOverdue !== undefined)
      params.append('isOverdue', filters.isOverdue.toString());
    if (filters.searchTerm) params.append('searchTerm', filters.searchTerm);
    if (filters.page !== undefined)
      params.append('page', filters.page.toString());
    if (filters.size !== undefined)
      params.append('size', filters.size.toString());

    return this.http.get<PageResponse<Deadline>>(
      `${this.deadlineUrl}?${params.toString()}`
    );
  }

  // Get deadlines for a specific client
  getClientDeadlines(
    clientId: number,
    filters: DeadlineFilters = {}
  ): Observable<PageResponse<Deadline>> {
    const params = new URLSearchParams();

    if (filters.status) params.append('status', filters.status);
    if (filters.priority) params.append('priority', filters.priority);
    if (filters.taskType) params.append('taskType', filters.taskType);
    if (filters.isOverdue !== undefined)
      params.append('isOverdue', filters.isOverdue.toString());
    if (filters.searchTerm) params.append('searchTerm', filters.searchTerm);
    if (filters.page !== undefined)
      params.append('page', filters.page.toString());
    if (filters.size !== undefined)
      params.append('size', filters.size.toString());

    return this.http.get<PageResponse<Deadline>>(
      `${this.deadlineUrl}/client/${clientId}?${params.toString()}`
    );
  }

  // Get overdue deadlines
  getOverdueDeadlines(): Observable<Deadline[]> {
    return this.http.get<Deadline[]>(`${this.deadlineUrl}/overdue`);
  }

  // Get due soon deadlines (within 7 days)
  getDueSoonDeadlines(): Observable<Deadline[]> {
    return this.http.get<Deadline[]>(`${this.deadlineUrl}/due-soon`);
  }

  // Get deadline statistics
  getDeadlineStatistics(): Observable<DeadlineStatistics> {
    return this.http.get<DeadlineStatistics>(`${this.deadlineUrl}/statistics`);
  }

  // Get deadline by ID
  getDeadlineById(id: number): Observable<Deadline> {
    return this.http.get<Deadline>(`${this.deadlineUrl}/${id}`);
  }

  // Update deadline status (this will update the underlying task)
  updateDeadlineStatus(
    deadlineId: number,
    status: string
  ): Observable<Deadline> {
    return this.http.put<Deadline>(`${this.deadlineUrl}/${deadlineId}/status`, {
      status,
    });
  }

  // Extend deadline
  extendDeadline(deadlineId: number, newDueDate: string): Observable<Deadline> {
    return this.http.put<Deadline>(`${this.deadlineUrl}/${deadlineId}/extend`, {
      newDueDate,
    });
  }

  // Get deadline history
  getDeadlineHistory(deadlineId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.deadlineUrl}/${deadlineId}/history`);
  }

  // Send deadline reminder
  sendDeadlineReminder(deadlineId: number): Observable<any> {
    return this.http.post<any>(`${this.deadlineUrl}/${deadlineId}/remind`, {});
  }

  // Get deadlines by date range
  getDeadlinesByDateRange(
    startDate: string,
    endDate: string
  ): Observable<Deadline[]> {
    return this.http.get<Deadline[]>(
      `${this.deadlineUrl}/date-range?startDate=${startDate}&endDate=${endDate}`
    );
  }

  // Get upcoming deadlines for dashboard
  getUpcomingDeadlines(limit: number = 10): Observable<Deadline[]> {
    return this.http.get<Deadline[]>(
      `${this.deadlineUrl}/upcoming?limit=${limit}`
    );
  }
}
