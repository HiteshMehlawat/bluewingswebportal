import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';
import { HttpParams } from '@angular/common/http';

export interface Task {
  id: number;
  title: string;
  description: string;
  clientId: number;
  clientName: string;
  assignedStaffId: number | null;
  assignedStaffName: string | null;
  assignedStaffEmployeeId: string | null;
  taskType:
    | 'ITR_FILING'
    | 'GST_FILING'
    | 'COMPANY_REGISTRATION'
    | 'TDS_FILING'
    | 'AUDIT'
    | 'OTHER';
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'ON_HOLD' | 'CANCELLED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  dueDate: string;
  assignedDate: string;
  startedDate: string | null;
  completedDate: string | null;
  estimatedHours: number | null;
  actualHours: number | null;
  createdBy: number;
  createdByName: string;
  createdByEmail: string;
  updatedBy: number | null;
  updatedByName: string | null;
  createdAt: string;
  updatedAt: string;
  deadlineStatus?: string;
  hasDocuments?: boolean;
  latestComment?: string;
  // Service information
  serviceItemId?: number;
  serviceItemName?: string;
  serviceCategoryName?: string;
  serviceSubcategoryName?: string;
}

export interface TaskDetail extends Task {
  clientPhone: string;
  clientEmail: string;
  assignedStaffPhone: string | null;
  assignedStaffEmail: string | null;
  // Service information is already included in the base Task interface
}

export interface TaskStatistics {
  totalTasks: number;
  pendingTasks: number;
  inProgressTasks: number;
  completedTasks: number;
  onHoldTasks: number;
  cancelledTasks: number;
  overdueTasks: number;
}

export interface StaffWorkload {
  staffId: number;
  staffName: string;
  employeeId: string;
  position: string;
  department: string;
  totalTasks: number;
  pendingTasks: number;
  inProgressTasks: number;
  completedTasks: number;
  overdueTasks: number;
  currentTasks: number;
}

export interface TaskFilters {
  status?: string;
  taskType?: string;
  priority?: string;
  assignedStaffId?: string;
  clientId?: string;
  searchTerm?: string;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  constructor(private http: HttpClient) {}

  // Get all tasks
  getTasks(filters: TaskFilters = {}): Observable<any> {
    let url = `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/filter`;
    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value.toString());
      }
    });

    if (params.toString()) {
      url += `?${params.toString()}`;
    }

    return this.http.get<any>(url);
  }

  // Search tasks
  searchTasks(
    searchTerm: string,
    page: number = 0,
    size: number = 10
  ): Observable<any> {
    const url = `${API_CONFIG.baseUrl}${
      API_CONFIG.tasks
    }/search?searchTerm=${encodeURIComponent(
      searchTerm
    )}&page=${page}&size=${size}`;
    return this.http.get<any>(url);
  }

  // Get task by ID
  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/${id}`
    );
  }

  // Get task detail by ID
  getTaskDetailById(id: number): Observable<TaskDetail> {
    return this.http.get<TaskDetail>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/${id}/detail`
    );
  }

  // Create new task
  createTask(task: Partial<Task>): Observable<Task> {
    return this.http.post<Task>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}`,
      task
    );
  }

  // Update task
  updateTask(id: number, task: Partial<Task>): Observable<Task> {
    return this.http.put<Task>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/${id}`,
      task
    );
  }

  // Delete task
  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/${id}`
    );
  }

  // Get overdue tasks
  getOverdueTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/overdue`
    );
  }

  // Get tasks due soon
  getTasksDueSoon(): Observable<Task[]> {
    return this.http.get<Task[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/due-soon`
    );
  }

  // Get tasks by staff
  getTasksByStaff(
    staffId: number,
    page: number = 0,
    size: number = 10
  ): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/staff/${staffId}`,
      { params }
    );
  }

  // Get tasks by staff and client
  getTasksByStaffAndClient(
    staffId: number,
    clientId: number,
    page: number = 0,
    size: number = 10
  ): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/staff/${staffId}/client/${clientId}`,
      { params }
    );
  }

  // Get task statistics
  getTaskStatistics(): Observable<TaskStatistics> {
    return this.http.get<TaskStatistics>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/statistics`
    );
  }

  // Get staff workload summary
  getStaffWorkloadSummary(): Observable<StaffWorkload[]> {
    return this.http.get<StaffWorkload[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/workload-summary`
    );
  }

  // Get available staff for assignment
  getAvailableStaffForAssignment(): Observable<StaffWorkload[]> {
    return this.http.get<StaffWorkload[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/available-staff`
    );
  }

  // Reassign task
  reassignTask(taskId: number, newStaffId: number): Observable<Task> {
    return this.http.put<Task>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/${taskId}/reassign?newStaffId=${newStaffId}`,
      {}
    );
  }

  // Update task status
  updateTaskStatus(
    taskId: number,
    newStatus: Task['status']
  ): Observable<Task> {
    return this.http.put<Task>(
      `${API_CONFIG.baseUrl}${API_CONFIG.tasks}/${taskId}/status?newStatus=${newStatus}`,
      {}
    );
  }
}
