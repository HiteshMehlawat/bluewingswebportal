import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

export interface ServiceRequest {
  id?: number;
  requestId?: string;
  serviceCategoryName?: string;
  serviceSubcategoryName?: string;
  serviceItemId?: number;
  serviceItemName?: string;
  description?: string;
  notes?: string;
  preferredDeadline?: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status?:
    | 'PENDING'
    | 'ASSIGNED'
    | 'IN_PROGRESS'
    | 'COMPLETED'
    | 'CANCELLED'
    | 'REJECTED';
  rejectionReason?: string;
  adminNotes?: string;
  staffNotes?: string;
  estimatedPrice?: number;
  finalPrice?: number;
  assignedDate?: string;
  completedDate?: string;
  rejectedDate?: string;
  clientId?: number;
  clientName?: string;
  clientEmail?: string;
  clientPhone?: string;
  companyName?: string;
  assignedStaffId?: number;
  assignedStaffName?: string;
  assignedStaffEmail?: string;
  assignedStaffEmployeeId?: string;
  acceptedById?: number;
  acceptedByName?: string;
  assignedById?: number;
  assignedByName?: string;
  rejectedById?: number;
  rejectedByName?: string;
  createdById?: number;
  createdByName?: string;
  updatedById?: number;
  updatedByName?: string;
  createdAt?: string;
  updatedAt?: string;
  deadlineStatus?: string;
  canEdit?: boolean;
  canDelete?: boolean;
  canAssign?: boolean;
  canReject?: boolean;
  canAccept?: boolean;
  canConvertToTask?: boolean;
}

export interface ServiceRequestDetail extends ServiceRequest {
  clientAddress?: string;
  clientCity?: string;
  clientState?: string;
  clientPincode?: string;
  clientGstNumber?: string;
  clientPanNumber?: string;
  assignedStaffPhone?: string;
  assignedStaffPosition?: string;
  assignedStaffDepartment?: string;
  acceptedByEmail?: string;
  assignedByEmail?: string;
  rejectedByEmail?: string;
  createdByEmail?: string;
  updatedByEmail?: string;
  statusBadgeClass?: string;
  priorityBadgeClass?: string;
}

export interface ServiceRequestStatistics {
  totalRequests?: number;
  pendingRequests?: number;
  assignedRequests?: number;
  inProgressRequests?: number;
  completedRequests?: number;
  rejectedRequests?: number;
  cancelledRequests?: number;
  overdueRequests?: number;
}

@Injectable({
  providedIn: 'root',
})
export class ServiceRequestService {
  private apiUrl = `${API_CONFIG.baseUrl}/api/service-requests`;

  constructor(private http: HttpClient) {}

  // Basic CRUD operations
  createServiceRequest(
    serviceRequest: ServiceRequest
  ): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(this.apiUrl, serviceRequest);
  }

  updateServiceRequest(
    id: number,
    serviceRequest: ServiceRequest
  ): Observable<ServiceRequest> {
    return this.http.put<ServiceRequest>(
      `${this.apiUrl}/${id}`,
      serviceRequest
    );
  }

  deleteServiceRequest(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getServiceRequestById(id: number): Observable<ServiceRequestDetail> {
    return this.http.get<ServiceRequestDetail>(`${this.apiUrl}/${id}`);
  }

  getServiceRequestByRequestId(
    requestId: string
  ): Observable<ServiceRequestDetail> {
    return this.http.get<ServiceRequestDetail>(
      `${this.apiUrl}/by-request-id/${requestId}`
    );
  }

  // Paginated listing with filters
  getAllServiceRequests(
    page: number = 0,
    size: number = 10,
    search?: string,
    statusFilter?: string,
    priorityFilter?: string
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }
    if (statusFilter) {
      params = params.set('statusFilter', statusFilter);
    }
    if (priorityFilter) {
      params = params.set('priorityFilter', priorityFilter);
    }

    return this.http.get<any>(this.apiUrl, { params });
  }

  // Client-specific operations
  getClientServiceRequests(
    clientId: number,
    page: number = 0,
    size: number = 10,
    search?: string,
    statusFilter?: string
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }
    if (statusFilter) {
      params = params.set('statusFilter', statusFilter);
    }

    return this.http.get<any>(`${this.apiUrl}/client/${clientId}`, { params });
  }

  getMyServiceRequests(
    page: number = 0,
    size: number = 10,
    search?: string,
    statusFilter?: string
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }
    if (statusFilter) {
      params = params.set('statusFilter', statusFilter);
    }

    return this.http.get<any>(`${this.apiUrl}/my-requests`, { params });
  }

  // Staff-specific operations
  getAssignedServiceRequests(
    staffId: number,
    page: number = 0,
    size: number = 10,
    search?: string,
    statusFilter?: string
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }
    if (statusFilter) {
      params = params.set('statusFilter', statusFilter);
    }

    return this.http.get<any>(`${this.apiUrl}/assigned/${staffId}`, { params });
  }

  getMyAssignedServiceRequests(
    page: number = 0,
    size: number = 10,
    search?: string,
    statusFilter?: string
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }
    if (statusFilter) {
      params = params.set('statusFilter', statusFilter);
    }

    return this.http.get<any>(`${this.apiUrl}/my-assigned`, { params });
  }

  // Admin operations
  assignServiceRequestToStaff(
    requestId: number,
    staffId: number,
    adminNotes?: string
  ): Observable<ServiceRequest> {
    let params = new HttpParams().set('staffId', staffId.toString());
    if (adminNotes) {
      params = params.set('adminNotes', adminNotes);
    }
    return this.http.post<ServiceRequest>(
      `${this.apiUrl}/${requestId}/assign`,
      null,
      { params }
    );
  }

  rejectServiceRequest(
    requestId: number,
    rejectionReason: string
  ): Observable<ServiceRequest> {
    const params = new HttpParams().set('rejectionReason', rejectionReason);
    return this.http.post<ServiceRequest>(
      `${this.apiUrl}/${requestId}/reject`,
      null,
      { params }
    );
  }

  updateServiceRequestStatus(
    requestId: number,
    status: string
  ): Observable<ServiceRequest> {
    const params = new HttpParams().set('status', status);
    return this.http.put<ServiceRequest>(
      `${this.apiUrl}/${requestId}/status`,
      null,
      { params }
    );
  }

  // Staff operations
  acceptServiceRequest(
    requestId: number,
    staffNotes?: string
  ): Observable<ServiceRequest> {
    let params = new HttpParams();
    if (staffNotes) {
      params = params.set('staffNotes', staffNotes);
    }
    return this.http.post<ServiceRequest>(
      `${this.apiUrl}/${requestId}/accept`,
      null,
      { params }
    );
  }

  convertServiceRequestToTask(
    requestId: number,
    taskData: {
      title: string;
      description: string;
      priority: string;
      dueDate: string;
      estimatedHours: number;
      assignedStaffId?: string | number | null;
    }
  ): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(
      `${this.apiUrl}/${requestId}/convert-to-task`,
      taskData
    );
  }

  // Statistics
  getServiceRequestStatistics(): Observable<ServiceRequestStatistics> {
    return this.http.get<ServiceRequestStatistics>(`${this.apiUrl}/statistics`);
  }

  getClientServiceRequestStatistics(
    clientId: number
  ): Observable<ServiceRequestStatistics> {
    return this.http.get<ServiceRequestStatistics>(
      `${this.apiUrl}/statistics/client/${clientId}`
    );
  }

  getStaffServiceRequestStatistics(
    staffId: number
  ): Observable<ServiceRequestStatistics> {
    return this.http.get<ServiceRequestStatistics>(
      `${this.apiUrl}/statistics/staff/${staffId}`
    );
  }

  // Utility methods
  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'ASSIGNED':
        return 'bg-blue-100 text-blue-800';
      case 'IN_PROGRESS':
        return 'bg-orange-100 text-orange-800';
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'CANCELLED':
        return 'bg-gray-100 text-gray-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getPriorityBadgeClass(priority: string): string {
    switch (priority) {
      case 'LOW':
        return 'bg-green-100 text-green-800';
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-800';
      case 'HIGH':
        return 'bg-orange-100 text-orange-800';
      case 'URGENT':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusDisplayName(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'Pending';
      case 'ASSIGNED':
        return 'Assigned';
      case 'IN_PROGRESS':
        return 'In Progress';
      case 'COMPLETED':
        return 'Completed';
      case 'CANCELLED':
        return 'Cancelled';
      case 'REJECTED':
        return 'Rejected';
      default:
        return status;
    }
  }

  getPriorityDisplayName(priority: string): string {
    switch (priority) {
      case 'LOW':
        return 'Low';
      case 'MEDIUM':
        return 'Medium';
      case 'HIGH':
        return 'High';
      case 'URGENT':
        return 'Urgent';
      default:
        return priority;
    }
  }
}
