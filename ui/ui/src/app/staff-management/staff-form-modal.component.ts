import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { StaffService, Staff } from '../services/staff.service';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../api.config';
import { PhoneInputComponent } from '../components/phone-input.component';
import { CountryDropdownComponent } from '../components/country-dropdown.component';

interface Supervisor {
  id: number;
  employeeId: string;
  firstName: string;
  lastName: string;
  position: string;
  department: string;
}

@Component({
  selector: 'app-staff-form-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, PhoneInputComponent],
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
            {{ isEditMode ? 'Edit Staff Member' : 'Add New Staff Member' }}
          </h2>
          <button (click)="close()" class="text-gray-400 hover:text-gray-600">
            <span class="material-icons text-2xl">close</span>
          </button>
        </div>

        <!-- Form -->
        <form [formGroup]="staffForm" (ngSubmit)="onSubmit()" class="p-6">
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
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    staffForm.get('firstName')?.invalid &&
                    staffForm.get('firstName')?.touched
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
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    staffForm.get('lastName')?.invalid &&
                    staffForm.get('lastName')?.touched
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
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    staffForm.get('email')?.invalid &&
                    staffForm.get('email')?.touched
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
                  [showError]="
                    (staffForm.get('phone')?.invalid &&
                      staffForm.get('phone')?.touched) ||
                    false
                  "
                  errorMessage="Phone number is required"
                >
                </app-phone-input>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Role *</label
                >
                <select
                  formControlName="role"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="STAFF">Staff</option>
                  <option value="ADMIN">Admin</option>
                </select>
                <div
                  *ngIf="
                    staffForm.get('role')?.invalid &&
                    staffForm.get('role')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Role is required
                </div>
              </div>
            </div>

            <!-- Professional Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-red-700 border-b pb-2">
                Professional Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Employee ID *</label
                >
                <input
                  type="text"
                  formControlName="employeeId"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    staffForm.get('employeeId')?.invalid &&
                    staffForm.get('employeeId')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Employee ID is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Position *</label
                >
                <input
                  type="text"
                  formControlName="position"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    staffForm.get('position')?.invalid &&
                    staffForm.get('position')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Position is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Department *</label
                >
                <div class="relative">
                  <input
                    type="text"
                    formControlName="department"
                    (input)="onDepartmentInput($event)"
                    (focus)="onDepartmentFocus()"
                    (blur)="onDepartmentBlur()"
                    (keydown)="onDepartmentKeydown($event)"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="Type to search or add new department"
                  />
                  <div
                    *ngIf="showDepartmentSuggestions"
                    class="absolute z-50 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                  >
                    <ul class="p-2">
                      <li
                        *ngFor="let dept of filteredDepartments; let i = index"
                        (click)="selectDepartment(dept)"
                        (mouseover)="selectedSuggestionIndex = i"
                        (mouseout)="selectedSuggestionIndex = -1"
                        class="cursor-pointer hover:bg-gray-100 py-1 px-2 rounded"
                        [class.bg-blue-100]="i === selectedSuggestionIndex"
                      >
                        {{ dept }}
                      </li>
                      <li
                        *ngIf="filteredDepartments.length === 0"
                        class="text-gray-500 py-1 px-2 text-sm"
                      >
                        No departments found. You can type a new department
                        name.
                      </li>
                    </ul>
                  </div>
                </div>
                <div
                  *ngIf="
                    staffForm.get('department')?.invalid &&
                    staffForm.get('department')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Department is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Joining Date *</label
                >
                <input
                  type="date"
                  formControlName="joiningDate"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    staffForm.get('joiningDate')?.invalid &&
                    staffForm.get('joiningDate')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Joining date is required
                </div>
              </div>
            </div>

            <!-- Employment Details -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-green-700 border-b pb-2">
                Employment Details
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Salary (₹)</label
                >
                <input
                  type="number"
                  formControlName="salary"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Supervisor</label
                >
                <select
                  formControlName="supervisorId"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Supervisor (Optional)</option>
                  <option
                    *ngFor="let supervisor of availableSupervisors"
                    [value]="supervisor.id"
                  >
                    {{ supervisor.firstName }} {{ supervisor.lastName }} -
                    {{ supervisor.position }}
                  </option>
                </select>
                <p class="text-xs text-gray-500 mt-1">
                  Select a supervisor for this staff member
                </p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Status</label
                >
                <div class="flex items-center space-x-4">
                  <label class="flex items-center">
                    <input
                      type="radio"
                      formControlName="isAvailable"
                      [value]="true"
                      class="mr-2 text-blue-600 focus:ring-blue-500"
                    />
                    <span class="text-sm">Available</span>
                  </label>
                  <label class="flex items-center">
                    <input
                      type="radio"
                      formControlName="isAvailable"
                      [value]="false"
                      class="mr-2 text-blue-600 focus:ring-blue-500"
                    />
                    <span class="text-sm">Unavailable</span>
                  </label>
                </div>
              </div>
            </div>

            <!-- Additional Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-purple-700 border-b pb-2">
                Additional Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Notes</label
                >
                <textarea
                  formControlName="notes"
                  rows="4"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Any additional notes about this staff member..."
                ></textarea>
              </div>

              <div *ngIf="isEditMode" class="bg-gray-50 rounded-lg p-4">
                <h4 class="font-medium text-gray-900 mb-2">
                  System Information
                </h4>
                <div class="text-sm text-gray-600 space-y-1">
                  <div>
                    Created:
                    {{
                      staff && staff.createdAt
                        ? (staff.createdAt | date : 'short')
                        : 'N/A'
                    }}
                  </div>
                  <div>
                    Last Updated:
                    {{
                      staff && staff.updatedAt
                        ? (staff.updatedAt | date : 'short')
                        : 'N/A'
                    }}
                  </div>
                  <div *ngIf="staff && staff.createdBy">
                    Created By: {{ staff.createdBy }}
                  </div>
                  <div *ngIf="staff && staff.updatedBy">
                    Updated By: {{ staff.updatedBy }}
                  </div>
                </div>
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
              Cancel
            </button>
            <button
              type="submit"
              [disabled]="staffForm.invalid || loading"
              class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span *ngIf="loading" class="inline-block animate-spin mr-2"
                >⟳</span
              >
              {{ isEditMode ? 'Update Staff' : 'Add Staff' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class StaffFormModalComponent implements OnInit {
  @Input() staff: Staff | null = null;
  @Output() saved = new EventEmitter<Staff>();
  @Output() cancelled = new EventEmitter<void>();

  staffForm: FormGroup;
  loading = false;
  isEditMode = false;
  availableSupervisors: Supervisor[] = [];
  departments: string[] = [];
  filteredDepartments: string[] = [];
  showDepartmentSuggestions = false;
  selectedSuggestionIndex = -1;

  constructor(
    private fb: FormBuilder,
    private staffService: StaffService,
    private http: HttpClient
  ) {
    this.staffForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      role: ['STAFF', Validators.required], // Default to STAFF
      employeeId: ['', Validators.required],
      position: ['', Validators.required],
      department: ['', Validators.required],
      joiningDate: ['', Validators.required],
      salary: [null],
      supervisorId: [null],
      isAvailable: [true],
      notes: [''],
    });
  }

  ngOnInit(): void {
    this.isEditMode = !!this.staff;
    if (this.staff) {
      this.staffForm.patchValue(this.staff);
    }
    this.loadSupervisors();
    this.loadDepartments();
  }

  loadSupervisors(): void {
    this.http
      .get<Supervisor[]>(`${API_CONFIG.baseUrl}${API_CONFIG.staff}`)
      .subscribe({
        next: (supervisors) => {
          // Filter out the current staff member if editing
          if (this.isEditMode && this.staff && this.staff.id) {
            this.availableSupervisors = supervisors.filter(
              (s) => s.id !== this.staff!.id
            );
          } else {
            this.availableSupervisors = supervisors;
          }
        },
        error: (error) => {
          console.error('Error loading supervisors:', error);
          this.availableSupervisors = [];
        },
      });
  }

  loadDepartments(): void {
    this.http
      .get<string[]>(`${API_CONFIG.baseUrl}${API_CONFIG.staff}/departments`)
      .subscribe({
        next: (departments) => {
          this.departments = departments;
          this.filteredDepartments = departments;
        },
        error: (error) => {
          console.error('Error loading departments:', error);
          this.departments = [];
          this.filteredDepartments = [];
        },
      });
  }

  onDepartmentInput(event: any): void {
    const value = event.target.value.toLowerCase();
    this.filteredDepartments = this.departments.filter((dept) =>
      dept.toLowerCase().includes(value)
    );
    this.showDepartmentSuggestions =
      value.length > 0 && this.filteredDepartments.length > 0;
    this.selectedSuggestionIndex = -1;
  }

  onDepartmentFocus(): void {
    const value = this.staffForm.get('department')?.value?.toLowerCase() || '';
    this.filteredDepartments = this.departments.filter((dept) =>
      dept.toLowerCase().includes(value)
    );
    this.showDepartmentSuggestions =
      value.length > 0 && this.filteredDepartments.length > 0;
    this.selectedSuggestionIndex = -1;
  }

  onDepartmentBlur(): void {
    // Delay hiding suggestions to allow for clicks
    setTimeout(() => {
      this.showDepartmentSuggestions = false;
      this.selectedSuggestionIndex = -1;
    }, 200);
  }

  onDepartmentKeydown(event: KeyboardEvent): void {
    if (!this.showDepartmentSuggestions) return;

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.selectedSuggestionIndex = Math.min(
          this.selectedSuggestionIndex + 1,
          this.filteredDepartments.length - 1
        );
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.selectedSuggestionIndex = Math.max(
          this.selectedSuggestionIndex - 1,
          -1
        );
        break;
      case 'Enter':
        event.preventDefault();
        if (
          this.selectedSuggestionIndex >= 0 &&
          this.selectedSuggestionIndex < this.filteredDepartments.length
        ) {
          this.selectDepartment(
            this.filteredDepartments[this.selectedSuggestionIndex]
          );
        }
        break;
      case 'Escape':
        this.showDepartmentSuggestions = false;
        this.selectedSuggestionIndex = -1;
        break;
    }
  }

  selectDepartment(dept: string): void {
    this.staffForm.get('department')?.setValue(dept);
    this.showDepartmentSuggestions = false;
    this.selectedSuggestionIndex = -1;
  }

  onSubmit(): void {
    if (this.staffForm.valid) {
      this.loading = true;
      const formData = this.staffForm.value;

      if (this.isEditMode && this.staff) {
        this.staffService.updateStaff(this.staff.id, formData).subscribe({
          next: (updatedStaff) => {
            this.loading = false;
            this.saved.emit(updatedStaff);
          },
          error: (error) => {
            this.loading = false;
            console.error('Error updating staff:', error);
            if (error.error && error.error.details) {
              alert('Error updating staff: ' + error.error.details);
            } else {
              alert(
                'Error updating staff. Please check the form data and try again.'
              );
            }
          },
        });
      } else {
        this.staffService.createStaff(formData).subscribe({
          next: (newStaff) => {
            this.loading = false;
            this.saved.emit(newStaff);
          },
          error: (error) => {
            this.loading = false;
            console.error('Error creating staff:', error);
            if (error.error && error.error.details) {
              alert('Error creating staff: ' + error.error.details);
            } else {
              alert(
                'Error creating staff. Please check the form data and try again.'
              );
            }
          },
        });
      }
    }
  }

  close(): void {
    this.cancelled.emit();
  }
}
