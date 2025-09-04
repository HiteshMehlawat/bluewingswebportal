import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../api.config';
import { ToastService } from '../services/toast.service';

interface Lead {
  id: number;
  leadId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  companyName?: string;
  serviceItemId?: number;
  serviceCategoryName?: string;
  serviceSubcategoryName?: string;
  serviceItemName?: string;
  serviceDescription?: string;
  source: string;
  status: string;
  priority: string;
  assignedStaffId?: number;
  assignedStaffName?: string;
  assignedStaffEmail?: string;
  assignedStaffPhone?: string;
  estimatedValue?: number;
  notes?: string;
  nextFollowUpDate?: string;
  lastContactDate?: string;
  convertedDate?: string;
  lostReason?: string;
  createdAt: string;
  updatedAt: string;
  createdByName?: string;
  updatedByName?: string;
}

interface Staff {
  id: number;
  userId: number;
  employeeId: string;
  position: string;
  department: string;
  firstName: string;
  lastName: string;
  email: string;
}

@Component({
  selector: 'app-staff-assignment-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    >
      <div
        class="bg-white rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
      >
        <!-- Header -->
        <div class="flex items-center justify-between p-6 border-b">
          <h2 class="text-xl font-semibold text-gray-800">
            Assign Staff to Lead
          </h2>
          <button (click)="close()" class="text-gray-400 hover:text-gray-600">
            <span class="material-icons text-2xl">close</span>
          </button>
        </div>

        <!-- Content -->
        <div class="p-6">
          <!-- Lead Information -->
          <div class="mb-6 p-4 bg-gray-50 rounded-lg">
            <h3 class="text-lg font-medium text-gray-800 mb-3">
              Lead Information
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p class="text-sm text-gray-600">Name</p>
                <p class="font-medium">
                  {{ lead?.firstName }} {{ lead?.lastName }}
                </p>
              </div>
              <div>
                <p class="text-sm text-gray-600">Email</p>
                <p class="font-medium">{{ lead?.email }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-600">Phone</p>
                <p class="font-medium">{{ lead?.phone }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-600">Company</p>
                <p class="font-medium">{{ lead?.companyName || 'N/A' }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-600">Service</p>
                <p class="font-medium">
                  {{ lead?.serviceCategoryName || 'Not specified' }}
                </p>
                <p
                  *ngIf="lead?.serviceSubcategoryName"
                  class="text-sm text-gray-500"
                >
                  {{ lead?.serviceSubcategoryName }}
                </p>
                <p *ngIf="lead?.serviceItemName" class="text-sm text-gray-500">
                  {{ lead?.serviceItemName }}
                </p>
              </div>
              <div>
                <p class="text-sm text-gray-600">Current Status</p>
                <span
                  class="inline-flex px-2 py-1 text-xs font-semibold rounded-full"
                  [class]="getStatusBadgeClass(lead?.status)"
                >
                  {{ lead?.status }}
                </span>
              </div>
            </div>
          </div>

          <!-- Current Assignment -->
          <div class="mb-6" *ngIf="lead?.assignedStaffName">
            <h3 class="text-lg font-medium text-gray-800 mb-3">
              Current Assignment
            </h3>
            <div class="p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <p class="text-blue-800">
                <span class="font-medium">Currently assigned to:</span>
                {{ lead?.assignedStaffName }}
              </p>
            </div>
          </div>

          <!-- Staff Selection -->
          <div class="mb-6">
            <h3 class="text-lg font-medium text-gray-800 mb-3">
              Select Staff Member
            </h3>

            <!-- Loading State -->
            <div *ngIf="loading" class="flex justify-center items-center py-8">
              <span class="text-lg text-gray-500"
                >Loading staff members...</span
              >
            </div>

            <!-- Staff Dropdown -->
            <div *ngIf="!loading && availableStaff.length > 0">
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Staff Member *</label
              >
              <select
                [(ngModel)]="selectedStaffId"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select Staff Member</option>
                <option *ngFor="let staff of availableStaff" [value]="staff.id">
                  {{ staff.firstName }} {{ staff.lastName }} -
                  {{ staff.position }} ({{ staff.employeeId }})
                </option>
              </select>
              <p class="text-xs text-gray-500 mt-1">
                Select the staff member to assign to this lead
              </p>
            </div>

            <!-- No Staff Available -->
            <div
              *ngIf="!loading && availableStaff.length === 0"
              class="text-center py-8"
            >
              <span class="material-icons text-gray-400 text-4xl mb-3"
                >people</span
              >
              <p class="text-gray-500">
                No staff members available for assignment.
              </p>
            </div>

            <!-- Error State -->
            <div
              *ngIf="error"
              class="p-4 bg-red-50 border border-red-200 rounded-lg"
            >
              <p class="text-red-800">{{ error }}</p>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex justify-end gap-4 p-6 border-t">
          <button
            type="button"
            (click)="close()"
            class="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            *ngIf="selectedStaffId"
            (click)="assignStaff()"
            [disabled]="assigning"
            class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span *ngIf="assigning" class="inline-block animate-spin mr-2"
              >⟳</span
            >
            Assign Staff
          </button>
        </div>
      </div>
    </div>
  `,
})
export class StaffAssignmentModalComponent implements OnInit {
  @Input() lead: Lead | null = null;
  @Output() assigned = new EventEmitter<Lead>();
  @Output() cancelled = new EventEmitter<void>();

  availableStaff: Staff[] = [];
  selectedStaffId: number | null = null;
  loading = false;
  assigning = false;
  error = '';

  constructor(private http: HttpClient, private toastService: ToastService) {}

  ngOnInit(): void {
    this.loadStaff();
    this.selectedStaffId = this.lead?.assignedStaffId ?? null;
  }

  loadStaff(): void {
    this.loading = true;
    this.error = '';

    this.http.get<Staff[]>(`${API_CONFIG.baseUrl}/api/staff`).subscribe({
      next: (staff) => {
        this.availableStaff = staff;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading staff:', error);
        this.error = 'Failed to load staff members. Please try again.';
        this.loading = false;
      },
    });
  }

  assignStaff(): void {
    if (!this.selectedStaffId || !this.lead) {
      return;
    }

    this.assigning = true;

    this.http
      .post<Lead>(
        `${API_CONFIG.baseUrl}/api/leads/${this.lead.id}/assign/${this.selectedStaffId}`,
        {}
      )
      .subscribe({
        next: (updatedLead) => {
          this.assigning = false;
          this.toastService.showSuccess('Staff assigned successfully!');
          this.assigned.emit(updatedLead);
        },
        error: (error) => {
          this.assigning = false;
          console.error('Error assigning staff:', error);
          this.toastService.showError(
            'Failed to assign staff. Please try again.'
          );
        },
      });
  }

  close(): void {
    this.cancelled.emit();
  }

  getStatusBadgeClass(status: string | undefined): string {
    if (!status) return 'bg-gray-100 text-gray-800';

    switch (status) {
      case 'NEW':
        return 'bg-blue-100 text-blue-800';
      case 'CONTACTED':
        return 'bg-yellow-100 text-yellow-800';
      case 'IN_DISCUSSION':
        return 'bg-purple-100 text-purple-800';
      case 'PROPOSAL_SENT':
        return 'bg-orange-100 text-orange-800';
      case 'CONVERTED':
        return 'bg-green-100 text-green-800';
      case 'LOST':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}
