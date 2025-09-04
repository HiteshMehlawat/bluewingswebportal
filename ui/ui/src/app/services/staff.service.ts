import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

export interface Staff {
  id: number;
  userId: number;
  employeeId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: string; // ADMIN, STAFF
  position: string;
  department: string;
  joiningDate: string;
  salary: number;
  supervisorId: number | null;
  supervisorName?: string;
  supervisorEmail?: string;
  supervisorPhone?: string;
  supervisorEmployeeId?: string;
  isAvailable: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  createdById: number;
  updatedBy: string;
  updatedById: number;
}

export interface StaffDetail extends Staff {
  supervisorName: string;
  supervisorEmail: string;
  supervisorPhone: string;
  supervisorEmployeeId: string;
  totalAssignedClients: number;
  totalAssignedTasks: number;
  totalCompletedTasks: number;
  totalPendingTasks: number;
  totalOverdueTasks: number;
  lastActivity: string;
  createdBy: string;
  createdById: number;
  updatedBy: string;
  updatedById: number;
}

export interface StaffPerformance {
  employeeId: string;
  name: string;
  position: string;
  department: string;
  supervisorId: number | null;
  totalAssigned: number;
  completed: number;
  pending: number;
  overdue: number;
  totalAssignedClients: number;
  lastActivity: string;
}

export interface StaffStats {
  totalStaff: number;
  activeStaff: number;
  inactiveStaff: number;
  staffByDepartment: { [key: string]: number };
}

@Injectable({
  providedIn: 'root',
})
export class StaffService {
  private baseUrl = API_CONFIG.baseUrl + API_CONFIG.staff;

  constructor(private http: HttpClient) {}

  getStaff(
    page: number = 0,
    size: number = 10,
    search?: string,
    departmentFilter?: string,
    statusFilter?: string
  ): Observable<{
    content: Staff[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  }> {
    let url = `${this.baseUrl}/paged?page=${page}&size=${size}`;

    if (search) {
      url += `&search=${encodeURIComponent(search)}`;
    }
    if (departmentFilter) {
      url += `&department=${encodeURIComponent(departmentFilter)}`;
    }
    if (statusFilter) {
      url += `&status=${encodeURIComponent(statusFilter)}`;
    }

    return this.http.get<{
      content: Staff[];
      totalElements: number;
      totalPages: number;
      size: number;
      number: number;
    }>(url);
  }

  getDepartments(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/departments`);
  }

  getStaffById(id: number): Observable<StaffDetail> {
    return this.http.get<StaffDetail>(`${this.baseUrl}/${id}`);
  }

  createStaff(staff: Partial<Staff>): Observable<Staff> {
    return this.http.post<Staff>(this.baseUrl, staff);
  }

  updateStaff(id: number, staff: Partial<Staff>): Observable<Staff> {
    return this.http.put<Staff>(`${this.baseUrl}/${id}`, staff);
  }

  deleteStaff(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  toggleStaffStatus(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/toggle-status`, {});
  }

  getStaffPerformance(id: number): Observable<StaffPerformance> {
    return this.http.get<StaffPerformance>(`${this.baseUrl}/${id}/performance`);
  }

  getStaffStats(): Observable<StaffStats> {
    return this.http.get<StaffStats>(`${this.baseUrl}/stats`);
  }

  getAllStaff(): Observable<Staff[]> {
    return this.http.get<Staff[]>(this.baseUrl);
  }

  getCurrentStaff(): Observable<Staff> {
    return this.http.get<Staff>(`${this.baseUrl}/me`);
  }

  updateMyProfile(profileData: Partial<Staff>): Observable<Staff> {
    return this.http.put<Staff>(`${this.baseUrl}/me`, profileData);
  }
}
