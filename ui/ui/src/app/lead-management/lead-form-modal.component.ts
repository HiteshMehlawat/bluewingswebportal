import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../api.config';
import { ToastService } from '../services/toast.service';
import { AuthService } from '../services/auth.service';
import { PhoneInputComponent } from '../components/phone-input.component';
import { ServiceSelectionComponent } from '../shared/service-selection/service-selection.component';

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
  selector: 'app-lead-form-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    PhoneInputComponent,
    ServiceSelectionComponent,
  ],
  template: `
    <div
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    >
      <div
        class="bg-white rounded-xl shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto"
      >
        <!-- Header -->
        <div class="flex items-center justify-between p-6 border-b">
          <h2 class="text-xl font-semibold text-gray-800">
            {{
              isEditMode
                ? isStaff
                  ? 'Update Lead Information'
                  : 'Edit Lead'
                : 'View Lead Details'
            }}
          </h2>
          <button (click)="close()" class="text-gray-400 hover:text-gray-600">
            <span class="material-icons text-2xl">close</span>
          </button>
        </div>

        <!-- Form -->
        <form [formGroup]="leadForm" (ngSubmit)="onSubmit()" class="p-6">
          <!-- Service Selection - Full Width -->
          <div class="mb-6">
            <h3 class="text-lg font-medium text-red-700 border-b pb-2 mb-4">
              Service Information
            </h3>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >Service Required *</label
              >
              <app-service-selection
                formControlName="serviceItemId"
                [required]="true"
                [showEstimatedHours]="true"
                [showServiceDescription]="false"
                [disabled]="!isEditMode || !canEditField('serviceItemId')"
                (serviceSelected)="onServiceSelected($event)"
              ></app-service-selection>
              <div
                *ngIf="isStaff && isEditMode && !canEditField('serviceItemId')"
                class="text-xs text-gray-500 mt-1"
              >
                <span class="material-icons text-xs align-text-bottom mr-1"
                  >lock</span
                >
                Read-only (Admin only)
              </div>
              <div
                *ngIf="
                  isEditMode &&
                  leadForm.get('serviceItemId')?.invalid &&
                  leadForm.get('serviceItemId')?.touched
                "
                class="text-red-500 text-sm mt-1"
              >
                Service is required
              </div>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Basic Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-blue-700 border-b pb-2">
                Basic Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >First Name *</label
                >
                <input
                  type="text"
                  formControlName="firstName"
                  [readonly]="!isEditMode || !canEditField('firstName')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="
                    !isEditMode || !canEditField('firstName')
                  "
                />
                <div
                  *ngIf="isStaff && isEditMode && !canEditField('firstName')"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    isEditMode &&
                    leadForm.get('firstName')?.invalid &&
                    leadForm.get('firstName')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  First name is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Last Name *</label
                >
                <input
                  type="text"
                  formControlName="lastName"
                  [readonly]="!isEditMode || !canEditField('lastName')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="!isEditMode || !canEditField('lastName')"
                />
                <div
                  *ngIf="isStaff && isEditMode && !canEditField('lastName')"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    isEditMode &&
                    leadForm.get('lastName')?.invalid &&
                    leadForm.get('lastName')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Last name is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Email *</label
                >
                <input
                  type="email"
                  formControlName="email"
                  [readonly]="!isEditMode || !canEditField('email')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="!isEditMode || !canEditField('email')"
                />
                <div
                  *ngIf="isStaff && isEditMode && !canEditField('email')"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    isEditMode &&
                    leadForm.get('email')?.invalid &&
                    leadForm.get('email')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Valid email is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Phone *</label
                >
                <app-phone-input
                  formControlName="phone"
                  placeholder="Enter phone number"
                  [disabled]="!isEditMode || !canEditField('phone')"
                  [showError]="
                    (isEditMode &&
                      leadForm.get('phone')?.invalid &&
                      leadForm.get('phone')?.touched) ||
                    false
                  "
                  errorMessage="Phone number is required"
                >
                </app-phone-input>
                <div
                  *ngIf="isStaff && isEditMode && !canEditField('phone')"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Company Name</label
                >
                <input
                  type="text"
                  formControlName="companyName"
                  [readonly]="!isEditMode || !canEditField('companyName')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="
                    !isEditMode || !canEditField('companyName')
                  "
                />
                <div
                  *ngIf="isStaff && isEditMode && !canEditField('companyName')"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
              </div>
            </div>

            <!-- Lead Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-red-700 border-b pb-2">
                Lead Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Service Description</label
                >
                <textarea
                  formControlName="serviceDescription"
                  rows="3"
                  [readonly]="
                    !isEditMode || !canEditField('serviceDescription')
                  "
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="
                    !isEditMode || !canEditField('serviceDescription')
                  "
                ></textarea>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Source</label
                >
                <select
                  formControlName="source"
                  [disabled]="!isEditMode || !canEditField('source')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="!isEditMode || !canEditField('source')"
                >
                  <option value="">Select Source</option>
                  <option value="WEBSITE">Website</option>
                  <option value="REFERRAL">Referral</option>
                  <option value="SOCIAL_MEDIA">Social Media</option>
                  <option value="COLD_CALL">Cold Call</option>
                  <option value="EMAIL">Email</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Estimated Value</label
                >
                <input
                  type="number"
                  formControlName="estimatedValue"
                  [readonly]="!isEditMode || !canEditField('estimatedValue')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="
                    !isEditMode || !canEditField('estimatedValue')
                  "
                  placeholder="Enter amount"
                />
              </div>
            </div>

            <!-- Status and Priority -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-pink-700 border-b pb-2">
                Status & Priority
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Status *</label
                >
                <select
                  formControlName="status"
                  [disabled]="!isEditMode || !canEditField('status')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="!isEditMode || !canEditField('status')"
                >
                  <option value="">Select Status</option>
                  <option value="NEW">New</option>
                  <option value="CONTACTED">Contacted</option>
                  <option value="IN_DISCUSSION">In Discussion</option>
                  <option value="PROPOSAL_SENT">Proposal Sent</option>
                  <option value="CONVERTED">Converted</option>
                  <option value="LOST">Lost</option>
                </select>
                <div
                  *ngIf="isStaff && isEditMode && !canEditField('status')"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    isEditMode &&
                    leadForm.get('status')?.invalid &&
                    leadForm.get('status')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Status is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Priority *</label
                >
                <select
                  formControlName="priority"
                  [disabled]="!isEditMode || !canEditField('priority')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="!isEditMode || !canEditField('priority')"
                >
                  <option value="">Select Priority</option>
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="URGENT">Urgent</option>
                </select>
                <div
                  *ngIf="isStaff && isEditMode && !canEditField('priority')"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    isEditMode &&
                    leadForm.get('priority')?.invalid &&
                    leadForm.get('priority')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Priority is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Next Follow-up Date</label
                >
                <input
                  type="date"
                  formControlName="nextFollowUpDate"
                  [readonly]="!isEditMode || !canEditField('nextFollowUpDate')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="
                    !isEditMode || !canEditField('nextFollowUpDate')
                  "
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Last Contact Date</label
                >
                <input
                  type="date"
                  formControlName="lastContactDate"
                  [readonly]="!isEditMode || !canEditField('lastContactDate')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="
                    !isEditMode || !canEditField('lastContactDate')
                  "
                />
              </div>

              <div *ngIf="leadForm.get('status')?.value === 'LOST'">
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Lost Reason</label
                >
                <textarea
                  formControlName="lostReason"
                  rows="2"
                  [readonly]="!isEditMode"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="!isEditMode"
                  placeholder="Enter reason for losing the lead"
                ></textarea>
              </div>
            </div>

            <!-- Staff Assignment -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-purple-700 border-b pb-2">
                Staff Assignment
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Assign Staff Member</label
                >
                <select
                  formControlName="assignedStaffId"
                  [disabled]="!isEditMode || !canEditField('assignedStaffId')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="
                    !isEditMode || !canEditField('assignedStaffId')
                  "
                >
                  <option value="">Select Staff Member (Optional)</option>
                  <option
                    *ngFor="let staff of availableStaff"
                    [value]="staff.id"
                  >
                    {{ staff.firstName }} {{ staff.lastName }} -
                    {{ staff.position }} ({{ staff.employeeId }})
                  </option>
                </select>
                <div
                  *ngIf="
                    isStaff && isEditMode && !canEditField('assignedStaffId')
                  "
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <p class="text-xs text-gray-500 mt-1">
                  You can assign a staff member now or do it later
                </p>
              </div>
            </div>

            <!-- Notes -->
            <div class="space-y-4 md:col-span-2">
              <h3 class="text-lg font-medium text-green-700 border-b pb-2">
                Notes
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Additional Notes</label
                >
                <textarea
                  formControlName="notes"
                  rows="4"
                  [readonly]="!isEditMode || !canEditField('notes')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  [class.bg-gray-100]="!isEditMode || !canEditField('notes')"
                  placeholder="Enter any additional notes about this lead"
                ></textarea>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex justify-end gap-4 mt-8 pt-6 border-t">
            <button
              type="button"
              (click)="close()"
              class="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
            >
              {{ isEditMode ? 'Cancel' : 'Close' }}
            </button>
            <button
              *ngIf="isEditMode"
              type="submit"
              [disabled]="leadForm.invalid || loading"
              class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span *ngIf="loading" class="inline-block animate-spin mr-2"
                >⟳</span
              >
              {{ isStaff ? 'Update Information' : 'Update Lead' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class LeadFormModalComponent implements OnInit {
  @Input() lead: Lead | null = null;
  @Input() isEditMode = false;
  @Output() saved = new EventEmitter<Lead>();
  @Output() cancelled = new EventEmitter<void>();

  leadForm: FormGroup;
  loading = false;
  availableStaff: Staff[] = [];

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private toastService: ToastService,
    private authService: AuthService
  ) {
    this.leadForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      companyName: [''],
      serviceItemId: ['', Validators.required],
      serviceDescription: [''],
      source: [''],
      status: ['NEW', Validators.required],
      priority: ['MEDIUM', Validators.required],
      assignedStaffId: [null],
      estimatedValue: [null],
      notes: [''],
      nextFollowUpDate: [''],
      lastContactDate: [''],
      lostReason: [''],
    });
  }

  ngOnInit(): void {
    if (this.lead) {
      this.leadForm.patchValue(this.lead);

      // If we have service hierarchy data, create a service item object for the component
      if (
        this.lead.serviceItemId &&
        this.lead.serviceCategoryName &&
        this.lead.serviceSubcategoryName &&
        this.lead.serviceItemName
      ) {
        this.selectedServiceItem = {
          id: this.lead.serviceItemId,
          categoryName: this.lead.serviceCategoryName,
          subcategoryName: this.lead.serviceSubcategoryName,
          name: this.lead.serviceItemName,
        };
      }
    }
    this.loadStaff();
  }

  get isStaff(): boolean {
    return this.authService.isStaff();
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  // For staff users, only allow editing specific fields
  canEditField(fieldName: string): boolean {
    if (this.isAdmin) return true;
    if (!this.isStaff) return false;

    // Staff CAN edit these fields:
    // - Status (to allow conversion)
    // - Next Follow-up Date
    // - Notes
    // - Service Item (serviceItemId)
    // - Service Description
    // - Source
    // - Estimated Value
    // - Last Contact Date
    const staffEditableFields = [
      'status',
      'nextFollowUpDate',
      'notes',
      'serviceItemId',
      'serviceDescription',
      'source',
      'estimatedValue',
      'lastContactDate',
    ];

    // Staff CANNOT edit these fields:
    // - firstName, lastName, email, phone, companyName (personal info)
    // - priority (priority)
    // - assignedStaffId (staff assignment)
    const staffNonEditableFields = [
      'firstName',
      'lastName',
      'email',
      'phone',
      'companyName',
      'priority',
      'assignedStaffId',
    ];

    // If it's in the non-editable list, return false
    if (staffNonEditableFields.includes(fieldName)) {
      return false;
    }

    // If it's in the editable list, return true
    return staffEditableFields.includes(fieldName);
  }

  selectedServiceItem: any = null;

  onServiceSelected(serviceItem: any): void {
    this.selectedServiceItem = serviceItem;
  }

  loadStaff(): void {
    this.http.get<Staff[]>(`${API_CONFIG.baseUrl}/api/staff`).subscribe({
      next: (staff) => {
        this.availableStaff = staff;
      },
      error: (error) => {
        console.error('Error loading staff:', error);
        this.availableStaff = [];
      },
    });
  }

  onSubmit(): void {
    if (this.leadForm.valid && this.isEditMode) {
      this.loading = true;
      const formData = this.leadForm.value;

      // Add service hierarchy information to the request
      if (this.selectedServiceItem) {
        formData.serviceCategoryName = this.selectedServiceItem.categoryName;
        formData.serviceSubcategoryName =
          this.selectedServiceItem.subcategoryName;
        formData.serviceItemName = this.selectedServiceItem.name;
      }

      // Use different endpoints for admin vs staff
      const endpoint = this.isStaff
        ? `${API_CONFIG.baseUrl}/api/leads/${this.lead!.id}/staff-update`
        : `${API_CONFIG.baseUrl}/api/leads/${this.lead!.id}`;

      this.http.put<Lead>(endpoint, formData).subscribe({
        next: (updatedLead) => {
          this.loading = false;
          this.toastService.showSuccess('Lead updated successfully!');
          this.saved.emit(updatedLead);
        },
        error: (error) => {
          this.loading = false;
          console.error('Error updating lead:', error);
          this.toastService.showError(
            error.error || 'Failed to update lead. Please try again.'
          );
        },
      });
    }
  }

  close(): void {
    this.cancelled.emit();
  }
}
