import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

// Admin/Staff interfaces for client management
export interface Client {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  isActive: boolean;
  emailVerified: boolean;
  lastLogin: string;
  companyName: string;
  companyType: string;
  panNumber: string;
  gstNumber: string;
  address: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
  businessType: string;
  industry: string;
  website: string;
  contactPerson: string;
  contactPhone: string;
  contactEmail: string;
  emergencyContact: string;
  clientType: string;
  registrationDate: string;
  createdAt: string;
  updatedAt: string;
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  assignedStaffName: string;
  assignedStaffId: number;
}

export interface ClientDetail {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  companyName: string;
  companyType: string;
  gstNumber: string;
  panNumber: string;
  address: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
  businessType: string;
  industry: string;
  website: string;
  contactPerson: string;
  contactPhone: string;
  contactEmail: string;
  emergencyContact: string;
  clientType: string;
  registrationDate: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  assignedStaffId: number;
  assignedStaffEmployeeId: string;
  assignedStaffName: string;
  assignedStaffEmail: string;
  assignedStaffPhone: string;
  createdBy: string;
  createdById: number;
  updatedBy: string;
  updatedById: number;
  totalDocuments: number;
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  overdueTasks: number;
}

export interface ClientDocument {
  documentId: number;
  documentName: string;
  originalFileName: string;
  uploadDateTime: string;
  fileType: string;
  fileSize: number;
  documentType: string;
  isVerified: boolean;
  uploadedByName: string;
  uploadedByRole: string;
  taskName: string;
}

export interface ClientProfile {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  companyName: string;
  address: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
  gstNumber?: string;
  panNumber?: string;
  registrationDate: string;
  status: string;
  clientType?: string;
  industry?: string;
}

export interface ClientActivity {
  id: number;
  activityType: string;
  description: string;
  details: string;
  activityDate: string;
  performedBy: string;
  performedByRole: string;
  relatedEntity: string;
  relatedEntityId: number;
  ipAddress: string;
  userAgent: string;
}

export interface ClientStats {
  totalClients: number;
  activeClients: number;
  inactiveClients: number;
  clientsWithAssignedStaff: number;
}

// Client-specific interfaces
export interface ClientDashboardStats {
  totalTasks: number;
  pendingTasks: number;
  completedTasks: number;
  overdueTasks: number;
  pendingDocuments: number;
  verifiedDocuments: number;
  upcomingDeadlines: number;
}

// Client-specific interfaces (renamed to avoid conflicts)
export interface ClientTask {
  id: number;
  title: string;
  description: string;
  clientId: number;
  clientName: string;
  clientPhone: string;
  clientEmail: string;
  assignedStaffId: number;
  assignedStaffName: string;
  assignedStaffEmployeeId: string;
  assignedStaffPhone: string;
  assignedStaffEmail: string;
  taskType: string;
  status: string;
  priority: string;
  dueDate: string;
  assignedDate: string;
  startedDate: string;
  completedDate: string;
  estimatedHours: number;
  actualHours: number;
  createdBy: number;
  createdByName: string;
  updatedBy: number;
  updatedByName: string;
  createdAt: string;
  updatedAt: string;
  deadlineStatus: string;
  hasDocuments: boolean;
  latestComment: string;
}

export interface ClientOwnDocument {
  id: number;
  fileName: string;
  originalFileName: string;
  documentType: string;
  status: 'PENDING' | 'VERIFIED' | 'REJECTED';
  uploadedAt: string;
  verifiedAt?: string;
  rejectionReason?: string;
  fileSize: number;
}

export interface RecentActivity {
  id: number;
  type:
    | 'TASK_CREATED'
    | 'TASK_UPDATED'
    | 'DOCUMENT_UPLOADED'
    | 'DOCUMENT_VERIFIED'
    | 'DEADLINE_APPROACHING';
  title: string;
  description: string;
  timestamp: string;
  relatedId?: number;
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
  // Calculated fields for frontend
  daysRemaining?: number;
  isOverdue?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class ClientService {
  private baseUrl = API_CONFIG.baseUrl;

  constructor(private http: HttpClient) {}

  // Get client dashboard statistics
  getDashboardStats(): Observable<ClientDashboardStats> {
    return this.http.get<ClientDashboardStats>(
      `${this.baseUrl}/api/clients/me/dashboard-stats`
    );
  }

  // Get client tasks (client view)
  getMyTasks(
    page: number = 0,
    size: number = 10
  ): Observable<{
    content: ClientTask[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
  }> {
    return this.http.get<{
      content: ClientTask[];
      totalElements: number;
      totalPages: number;
      pageNumber: number;
      pageSize: number;
    }>(`${this.baseUrl}/api/clients/me/tasks?page=${page}&size=${size}`);
  }

  // Get client tasks with filters (client view) - now returns detailed information
  getMyTasksWithFilters(filters: {
    status?: string;
    taskType?: string;
    priority?: string;
    searchTerm?: string;
    page: number;
    size: number;
  }): Observable<{
    content: ClientTask[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
  }> {
    let url = `${this.baseUrl}/api/clients/me/tasks/filter?page=${filters.page}&size=${filters.size}`;

    if (filters.status) {
      url += `&status=${encodeURIComponent(filters.status)}`;
    }
    if (filters.taskType) {
      url += `&taskType=${encodeURIComponent(filters.taskType)}`;
    }
    if (filters.priority) {
      url += `&priority=${encodeURIComponent(filters.priority)}`;
    }
    if (filters.searchTerm) {
      url += `&searchTerm=${encodeURIComponent(filters.searchTerm)}`;
    }

    return this.http.get<{
      content: ClientTask[];
      totalElements: number;
      totalPages: number;
      pageNumber: number;
      pageSize: number;
    }>(url);
  }

  // Get client documents (client view)
  getMyDocuments(
    page: number = 0,
    size: number = 10
  ): Observable<{
    content: ClientOwnDocument[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
  }> {
    return this.http.get<{
      content: ClientOwnDocument[];
      totalElements: number;
      totalPages: number;
      pageNumber: number;
      pageSize: number;
    }>(`${this.baseUrl}/api/clients/me/documents?page=${page}&size=${size}`);
  }

  // Get recent activities
  getRecentActivities(limit: number = 10): Observable<RecentActivity[]> {
    return this.http.get<RecentActivity[]>(
      `${this.baseUrl}/api/clients/me/activities?limit=${limit}`
    );
  }

  // Get upcoming deadlines
  getUpcomingDeadlines(): Observable<UpcomingDeadline[]> {
    return this.http.get<UpcomingDeadline[]>(
      `${this.baseUrl}/api/clients/me/deadlines`
    );
  }

  // Upload document
  uploadDocument(
    file: File,
    documentType: string,
    taskId?: number
  ): Observable<ClientOwnDocument> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);
    if (taskId) {
      formData.append('taskId', taskId.toString());
    }

    return this.http.post<ClientOwnDocument>(
      `${this.baseUrl}/api/clients/me/documents`,
      formData
    );
  }

  // Download document
  downloadDocument(documentId: number): Observable<Blob> {
    return this.http.get(
      `${this.baseUrl}/api/clients/me/documents/${documentId}/download`,
      {
        responseType: 'blob',
      }
    );
  }

  // Get client profile
  getClientProfile(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/clients/me`);
  }

  // Update client profile
  updateClientProfile(profileData: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/clients/me`, profileData);
  }

  // Send message to staff
  sendMessage(
    taskId: number,
    message: string,
    attachments?: File[]
  ): Observable<any> {
    const formData = new FormData();
    formData.append('taskId', taskId.toString());
    formData.append('message', message);

    if (attachments) {
      attachments.forEach((file) => {
        formData.append('attachments', file);
      });
    }

    return this.http.post(`${this.baseUrl}/api/clients/me/messages`, formData);
  }

  // Request callback
  requestCallback(callbackData: {
    preferredDate: string;
    preferredTime: string;
    reason: string;
    notes?: string;
  }): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/api/clients/me/callbacks`,
      callbackData
    );
  }

  // Acknowledge task completion
  acknowledgeTaskCompletion(taskId: number): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/api/clients/me/tasks/${taskId}/acknowledge`,
      {}
    );
  }

  // ===== ADMIN/STAFF METHODS FOR CLIENT MANAGEMENT =====

  // Get all clients with pagination and search
  getClients(
    page: number = 0,
    size: number = 10,
    search?: string,
    statusFilter?: string
  ): Observable<{
    content: Client[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  }> {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (search) params.append('search', search);
    if (statusFilter) params.append('statusFilter', statusFilter);

    return this.http.get<{
      content: Client[];
      totalElements: number;
      totalPages: number;
      size: number;
      number: number;
    }>(`${this.baseUrl}${API_CONFIG.clients}?${params.toString()}`);
  }

  // Get client by ID with detailed information
  getClientById(id: number): Observable<ClientDetail> {
    return this.http.get<ClientDetail>(
      `${this.baseUrl}${API_CONFIG.clients}/${id}`
    );
  }

  // Create new client
  createClient(client: Partial<Client>): Observable<Client> {
    return this.http.post<Client>(
      `${this.baseUrl}${API_CONFIG.clients}`,
      client
    );
  }

  // Update client
  updateClient(id: number, client: Partial<Client>): Observable<Client> {
    return this.http.put<Client>(
      `${this.baseUrl}${API_CONFIG.clients}/${id}`,
      client
    );
  }

  // Delete client (soft delete)
  deleteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}${API_CONFIG.clients}/${id}`);
  }

  // Assign staff to client
  assignStaffToClient(clientId: number, staffId: number): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}${API_CONFIG.clients}/${clientId}/assign-staff/${staffId}`,
      {}
    );
  }

  // Toggle client status
  toggleClientStatus(clientId: number): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}${API_CONFIG.clients}/${clientId}/toggle-status`,
      {}
    );
  }

  // Get client documents (admin view)
  getClientDocuments(
    clientId: number,
    page: number = 0,
    size: number = 10
  ): Observable<{
    content: ClientDocument[];
    totalElements: number;
    totalPages: number;
  }> {
    return this.http.get<any>(
      `${this.baseUrl}${API_CONFIG.clients}/${clientId}/documents?page=${page}&size=${size}`
    );
  }

  // Get client activity (admin view)
  getClientActivity(
    clientId: number,
    page: number = 0,
    size: number = 10
  ): Observable<{
    content: ClientActivity[];
    totalElements: number;
    totalPages: number;
  }> {
    return this.http.get<any>(
      `${this.baseUrl}${API_CONFIG.clients}/${clientId}/activity?page=${page}&size=${size}`
    );
  }

  // Get client tasks (admin view)
  getClientTasks(
    clientId: number,
    page: number = 0,
    size: number = 10
  ): Observable<{
    content: ClientTask[];
    totalElements: number;
    totalPages: number;
  }> {
    return this.http.get<any>(
      `${this.baseUrl}${API_CONFIG.clients}/${clientId}/tasks?page=${page}&size=${size}`
    );
  }

  // Get client statistics
  getClientStats(): Observable<ClientStats> {
    return this.http.get<ClientStats>(
      `${this.baseUrl}${API_CONFIG.clients}/stats`
    );
  }

  // Profile methods for clients
  getMyProfile(): Observable<ClientProfile> {
    return this.http.get<ClientProfile>(
      `${this.baseUrl}/api/clients/me/profile`
    );
  }

  updateMyProfile(
    profileData: Partial<ClientProfile>
  ): Observable<ClientProfile> {
    return this.http.put<ClientProfile>(
      `${this.baseUrl}/api/clients/me/profile`,
      profileData
    );
  }
}
