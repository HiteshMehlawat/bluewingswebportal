import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { StaffService, Staff, StaffDetail } from '../services/staff.service';
import { PaginationComponent } from '../pagination/pagination.component';
import { StaffFormModalComponent } from './staff-form-modal.component';
import { ConfirmDialogComponent } from '../components/confirm-dialog.component';
import { ToastService } from '../services/toast.service';
import { API_CONFIG } from '../api.config';

interface StaffPerformance {
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

@Component({
  selector: 'app-staff-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    PaginationComponent,
    StaffFormModalComponent,
    ConfirmDialogComponent,
  ],
  templateUrl: './staff-management.component.html',
  styleUrls: ['./staff-management.component.scss'],
})
export class StaffManagementComponent implements OnInit {
  staff: Staff[] = [];
  loading = false;
  error: string | null = null;

  // Pagination
  currentPage = 0;
  pageSize = 5;
  totalElements = 0;

  // Search and filters
  searchTerm = '';
  departmentFilter = '';
  statusFilter = '';
  departments: string[] = [];

  // Modal state
  showStaffModal = false;
  selectedStaff: Staff | null = null;
  showViewModal = false;
  showPerformanceModal = false;
  selectedStaffForView: StaffDetail | null = null;
  selectedStaffForPerformance: Staff | null = null;
  staffPerformance: StaffPerformance | null = null;

  // Delete confirmation dialog
  showDeleteDialog = false;
  staffToDelete: Staff | null = null;

  constructor(
    private staffService: StaffService,
    private http: HttpClient,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadStaff();
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.staffService.getDepartments().subscribe({
      next: (departments) => {
        this.departments = departments;
      },
      error: (error) => {
        console.error('Error loading departments:', error);
        this.departments = [];
      },
    });
  }

  loadStaff(): void {
    this.loading = true;
    this.error = null;

    this.staffService
      .getStaff(
        this.currentPage,
        this.pageSize,
        this.searchTerm,
        this.departmentFilter,
        this.statusFilter
      )
      .subscribe({
        next: (response: any) => {
          this.staff = response.content;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (error: any) => {
          this.error = 'Failed to load staff';
          this.loading = false;
          console.error('Error loading staff:', error);
        },
      });
  }

  onPageChange(page: number): void {
    this.currentPage = page - 1;
    this.loadStaff();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0; // Reset to first page
    this.loadStaff();
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadStaff();
  }

  onDepartmentFilterChange(): void {
    this.currentPage = 0;
    this.loadStaff();
  }

  onStatusFilterChange(): void {
    this.currentPage = 0;
    this.loadStaff();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.departmentFilter = '';
    this.statusFilter = '';
    this.currentPage = 0;
    this.loadStaff();
  }

  openAddStaffModal(): void {
    this.selectedStaff = null;
    this.showStaffModal = true;
  }

  openEditStaffModal(staff: Staff | StaffDetail): void {
    // Convert StaffDetail to Staff if needed
    const staffForEdit: Staff = {
      id: staff.id,
      userId: staff.userId,
      employeeId: staff.employeeId,
      firstName: staff.firstName,
      lastName: staff.lastName,
      email: staff.email,
      phone: staff.phone,
      role: staff.role,
      position: staff.position,
      department: staff.department,
      joiningDate: staff.joiningDate,
      salary: staff.salary,
      supervisorId: staff.supervisorId,
      isAvailable: staff.isAvailable,
      createdAt: staff.createdAt,
      updatedAt: staff.updatedAt,
      createdBy: staff.createdBy,
      createdById: staff.createdById,
      updatedBy: staff.updatedBy,
      updatedById: staff.updatedById,
    };

    this.selectedStaff = staffForEdit;
    this.showStaffModal = true;
  }

  onStaffSaved(staff: Staff): void {
    this.showStaffModal = false;
    this.selectedStaff = null;
    this.loadStaff(); // Refresh the list

    // Show success toast
    if (this.selectedStaff) {
      this.toastService.showSuccess('Staff updated successfully!');
    } else {
      this.toastService.showSuccess('Staff created successfully!');
    }
  }

  onStaffModalCancelled(): void {
    this.showStaffModal = false;
    this.selectedStaff = null;
  }

  viewStaffDetails(staff: Staff): void {
    this.staffService.getStaffById(staff.id).subscribe({
      next: (staffDetail: any) => {
        this.selectedStaffForView = staffDetail;
        this.showViewModal = true;

        // Also load performance data for the view modal
        this.loadStaffPerformanceForView(staff.id);
      },
      error: (error: any) => {
        console.error('Error fetching staff details:', error);
      },
    });
  }

  loadStaffPerformanceForView(staffId: number): void {
    this.http
      .get<StaffPerformance>(
        `${API_CONFIG.baseUrl}${API_CONFIG.staff}/${staffId}/performance`
      )
      .subscribe({
        next: (performance) => {
          if (this.selectedStaffForView) {
            // Update the view data with performance metrics
            this.selectedStaffForView.totalAssignedClients =
              performance.totalAssignedClients;
            this.selectedStaffForView.totalAssignedTasks =
              performance.totalAssigned;
            this.selectedStaffForView.totalCompletedTasks =
              performance.completed;
            this.selectedStaffForView.totalPendingTasks = performance.pending;
            this.selectedStaffForView.totalOverdueTasks = performance.overdue;
            this.selectedStaffForView.lastActivity = performance.lastActivity;
          }
        },
        error: (error: any) => {
          console.error('Error loading staff performance for view:', error);
        },
      });
  }

  viewStaffPerformance(staff: Staff): void {
    this.selectedStaffForPerformance = staff;
    this.loadStaffPerformance(staff.id);
    this.showPerformanceModal = true;
  }

  loadStaffPerformance(staffId: number): void {
    this.staffPerformance = null; // Reset performance data
    this.http
      .get<StaffPerformance>(
        `${API_CONFIG.baseUrl}${API_CONFIG.staff}/${staffId}/performance`
      )
      .subscribe({
        next: (performance) => {
          this.staffPerformance = performance;
        },
        error: (error: any) => {
          console.error('Error loading staff performance:', error);
          this.staffPerformance = null;
          // Show error message to user
          alert('Error loading performance data. Please try again.');
        },
      });
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedStaffForView = null;
  }

  closePerformanceModal(): void {
    this.showPerformanceModal = false;
    this.selectedStaffForPerformance = null;
    this.staffPerformance = null;
  }

  toggleStaffStatus(staff: Staff): void {
    const updatedStaff = { ...staff, isAvailable: !staff.isAvailable };
    this.staffService.updateStaff(staff.id, updatedStaff).subscribe({
      next: () => {
        this.loadStaff(); // Refresh the list
        this.toastService.showSuccess('Staff status updated successfully!');
      },
      error: (error: any) => {
        console.error('Error toggling staff status:', error);
        this.toastService.showError(
          'Failed to update staff status. Please try again.'
        );
      },
    });
  }

  deleteStaff(staff: Staff): void {
    this.staffToDelete = staff;
    this.showDeleteDialog = true;
  }

  confirmDelete(): void {
    if (this.staffToDelete) {
      this.staffService.deleteStaff(this.staffToDelete.id).subscribe({
        next: () => {
          this.showDeleteDialog = false;
          this.staffToDelete = null;
          this.loadStaff(); // Refresh the list
          this.toastService.showSuccess('Staff deleted successfully!');
        },
        error: (error: any) => {
          console.error('Error deleting staff:', error);
          this.showDeleteDialog = false;
          this.staffToDelete = null;
          this.toastService.showError(
            'Failed to delete staff. Please try again.'
          );
        },
      });
    }
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
    this.staffToDelete = null;
  }
}
