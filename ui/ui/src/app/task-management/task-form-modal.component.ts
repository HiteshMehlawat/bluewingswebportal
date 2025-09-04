import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TaskService, Task } from '../services/task.service';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../api.config';
import { AuthService } from '../services/auth.service';
import { ServiceSelectionComponent } from '../shared/service-selection/service-selection.component';
import {
  ServiceItem,
  ServiceHierarchyService,
} from '../services/service-hierarchy.service';

interface Client {
  id: number;
  firstName: string;
  lastName: string;
  companyName: string;
  email: string;
  phone: string;
  isActive: boolean;
}

interface Staff {
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

@Component({
  selector: 'app-task-form-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ServiceSelectionComponent],
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
            {{ isEditMode ? 'Edit Task' : 'Add New Task' }}
          </h2>
          <button (click)="close()" class="text-gray-400 hover:text-gray-600">
            <span class="material-icons text-2xl">close</span>
          </button>
        </div>

        <!-- Form -->
        <form [formGroup]="taskForm" (ngSubmit)="onSubmit()" class="p-6">
          <!-- Service Selection - Moved to top -->
          <div class="mb-6">
            <h3 class="text-lg font-medium text-green-700 border-b pb-2 mb-4">
              Service Selection
            </h3>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >Select Service *</label
              >
              <app-service-selection
                formControlName="serviceItemId"
                [required]="true"
                [showEstimatedHours]="true"
                [disabled]="isStaff && isEditMode"
                (serviceSelected)="onServiceSelected($event)"
              ></app-service-selection>
              <div
                *ngIf="
                  taskForm.get('serviceItemId')?.invalid &&
                  taskForm.get('serviceItemId')?.touched
                "
                class="text-red-500 text-sm mt-1"
              >
                Service selection is required
              </div>
              <p class="text-xs text-gray-500 mt-1">
                Select a service to auto-populate task details
              </p>
              <div
                *ngIf="isStaff && isEditMode"
                class="text-xs text-gray-500 mt-1"
              >
                <span class="material-icons text-xs align-text-bottom mr-1"
                  >lock</span
                >
                Read-only (Admin only)
              </div>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Basic Task Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-blue-700 border-b pb-2">
                Task Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Title *</label
                >
                <input
                  type="text"
                  formControlName="title"
                  [class]="
                    'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:border-transparent ' +
                    (taskForm.get('title')?.disabled
                      ? 'border-gray-200 bg-gray-50 text-gray-500 cursor-not-allowed'
                      : 'border-gray-300 focus:ring-blue-500')
                  "
                />
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    taskForm.get('title')?.invalid &&
                    taskForm.get('title')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Task title is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Description *</label
                >
                <textarea
                  formControlName="description"
                  rows="3"
                  [class]="
                    'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:border-transparent ' +
                    (taskForm.get('description')?.disabled
                      ? 'border-gray-200 bg-gray-50 text-gray-500 cursor-not-allowed'
                      : 'border-gray-300 focus:ring-blue-500')
                  "
                ></textarea>
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    taskForm.get('description')?.invalid &&
                    taskForm.get('description')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Task description is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Priority *</label
                >
                <select
                  formControlName="priority"
                  [class]="
                    'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:border-transparent ' +
                    (taskForm.get('priority')?.disabled
                      ? 'border-gray-200 bg-gray-50 text-gray-500 cursor-not-allowed'
                      : 'border-gray-300 focus:ring-blue-500')
                  "
                >
                  <option value="">Select Priority</option>
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="URGENT">Urgent</option>
                </select>
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    taskForm.get('priority')?.invalid &&
                    taskForm.get('priority')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Priority is required
                </div>
              </div>
            </div>

            <!-- Assignment Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-red-700 border-b pb-2">
                Assignment Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Client *</label
                >
                <select
                  formControlName="clientId"
                  [class]="
                    'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:border-transparent ' +
                    (taskForm.get('clientId')?.disabled
                      ? 'border-gray-200 bg-gray-50 text-gray-500 cursor-not-allowed'
                      : 'border-gray-300 focus:ring-blue-500')
                  "
                >
                  <option value="">Select Client</option>
                  <option *ngIf="loadingClients" disabled>
                    Loading clients...
                  </option>
                  <option *ngIf="!loadingClients && clientsError" disabled>
                    {{ clientsError }}
                  </option>
                  <option
                    *ngFor="let client of activeClients"
                    [value]="client.id"
                  >
                    {{ client.firstName }} {{ client.lastName }} -
                    {{ client.companyName }}
                  </option>
                </select>
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    taskForm.get('clientId')?.invalid &&
                    taskForm.get('clientId')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Client is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Assigned Staff</label
                >
                <select
                  formControlName="assignedStaffId"
                  [class]="
                    'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:border-transparent ' +
                    (taskForm.get('assignedStaffId')?.disabled
                      ? 'border-gray-200 bg-gray-50 text-gray-500 cursor-not-allowed'
                      : 'border-gray-300 focus:ring-blue-500')
                  "
                >
                  <option value="">Select Staff Member (Optional)</option>
                  <option *ngIf="loadingStaff" disabled>
                    Loading staff...
                  </option>
                  <option *ngIf="!loadingStaff && staffError" disabled>
                    {{ staffError }}
                  </option>
                  <option
                    *ngFor="let staff of sortedStaff"
                    [value]="staff.staffId"
                  >
                    {{ staff.staffName }} - {{ staff.position }} ({{
                      staff.employeeId
                    }})
                  </option>
                </select>
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <p class="text-xs text-gray-500 mt-1">
                  Staff are sorted by workload (least busy first). Employee ID
                  is shown in parentheses.
                </p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Status</label
                >
                <select
                  formControlName="status"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="PENDING">Pending</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="ON_HOLD">On Hold</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-green-600 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >edit</span
                  >
                  Editable (Staff can update)
                </div>
              </div>
            </div>

            <!-- Timeline Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-green-700 border-b pb-2">
                Timeline Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Due Date *</label
                >
                <input
                  type="date"
                  formControlName="dueDate"
                  [class]="
                    'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:border-transparent ' +
                    (taskForm.get('dueDate')?.disabled
                      ? 'border-gray-200 bg-gray-50 text-gray-500 cursor-not-allowed'
                      : 'border-gray-300 focus:ring-blue-500')
                  "
                />
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <div
                  *ngIf="
                    taskForm.get('dueDate')?.invalid &&
                    taskForm.get('dueDate')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Due date is required
                </div>
              </div>

              <div *ngIf="isEditMode">
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Started Date</label
                >
                <input
                  type="datetime-local"
                  formControlName="startedDate"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-green-600 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >edit</span
                  >
                  Editable (Staff can update)
                </div>
              </div>

              <div *ngIf="isEditMode">
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Completed Date</label
                >
                <input
                  type="datetime-local"
                  formControlName="completedDate"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-green-600 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >edit</span
                  >
                  Editable (Staff can update)
                </div>
              </div>
            </div>

            <!-- Time Tracking -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-purple-700 border-b pb-2">
                Time Tracking
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Estimated Hours</label
                >
                <input
                  type="number"
                  formControlName="estimatedHours"
                  step="0.5"
                  min="0"
                  [class]="
                    'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:border-transparent ' +
                    (taskForm.get('estimatedHours')?.disabled
                      ? 'border-gray-200 bg-gray-50 text-gray-500 cursor-not-allowed'
                      : 'border-gray-300 focus:ring-blue-500')
                  "
                />
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-gray-500 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >lock</span
                  >
                  Read-only (Admin only)
                </div>
                <p class="text-xs text-gray-500 mt-1">
                  Estimated time to complete the task
                </p>
              </div>

              <div *ngIf="isEditMode">
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Actual Hours</label
                >
                <input
                  type="number"
                  formControlName="actualHours"
                  step="0.5"
                  min="0"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="isStaff && isEditMode"
                  class="text-xs text-green-600 mt-1"
                >
                  <span class="material-icons text-xs align-text-bottom mr-1"
                    >edit</span
                  >
                  Editable (Staff can update)
                </div>
                <p class="text-xs text-gray-500 mt-1">
                  Actual time spent on the task
                </p>
              </div>
            </div>

            <!-- Additional Information -->
            <!-- <div class="space-y-4">
              <h3 class="text-lg font-medium text-yellow-700 border-b pb-2">
                Additional Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Notes</label
                >
                <textarea
                  formControlName="notes"
                  rows="3"
                  placeholder="Additional notes or comments..."
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                ></textarea>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Tags</label
                >
                <input
                  type="text"
                  formControlName="tags"
                  placeholder="Enter tags separated by commas..."
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <p class="text-xs text-gray-500 mt-1">
                  Use tags to categorize and filter tasks
                </p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Reference Number</label
                >
                <input
                  type="text"
                  formControlName="referenceNumber"
                  placeholder="Internal reference number..."
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div> -->
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
              [disabled]="taskForm.invalid || loading"
              class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span *ngIf="loading" class="inline-block animate-spin mr-2"
                >⟳</span
              >
              {{ isEditMode ? 'Update Task' : 'Create Task' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class TaskFormModalComponent implements OnInit {
  @Input() task: Task | null = null;
  @Output() saved = new EventEmitter<Task>();
  @Output() cancelled = new EventEmitter<void>();

  taskForm: FormGroup;
  loading = false;
  isEditMode = false;
  availableClients: Client[] = [];
  availableStaff: Staff[] = [];
  loadingClients = false;
  loadingStaff = false;
  clientsError = '';
  staffError = '';

  // Role-based properties
  isStaff = false;
  isAdmin = false;
  isClient = false;

  // Service selection
  selectedServiceItem: ServiceItem | null = null;

  onServiceSelected(serviceItem: ServiceItem): void {
    this.selectedServiceItem = serviceItem;

    // Always auto-populate fields when a service is selected
    if (serviceItem) {
      this.taskForm.patchValue({
        title: serviceItem.name,
        description: serviceItem.description || `Service: ${serviceItem.name}`,
        estimatedHours:
          serviceItem.estimatedHours > 0 ? serviceItem.estimatedHours : null,
        taskType: 'OTHER', // Set to OTHER to avoid enum validation issues
      });
    }
  }

  get activeClients(): Client[] {
    return this.availableClients.filter((client) => client.isActive);
  }

  get sortedStaff(): Staff[] {
    return this.availableStaff.sort((a, b) => a.currentTasks - b.currentTasks);
  }

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private http: HttpClient,
    private authService: AuthService,
    private serviceHierarchyService: ServiceHierarchyService
  ) {
    this.taskForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      clientId: ['', Validators.required],
      assignedStaffId: [null],
      taskType: ['OTHER', Validators.required], // Set default to OTHER for backward compatibility
      serviceItemId: [null, Validators.required], // New service selection
      status: ['PENDING'],
      priority: ['', Validators.required],
      dueDate: ['', Validators.required],
      startedDate: [null],
      completedDate: [null],
      estimatedHours: [null],
      actualHours: [null],
      notes: [''],
      tags: [''],
      referenceNumber: [''],
    });
  }

  ngOnInit(): void {
    this.isEditMode = !!this.task;

    // Detect user role
    this.isStaff = this.authService.isStaff();
    this.isAdmin = this.authService.isAdmin();
    this.isClient = this.authService.isClient();

    if (this.task) {
      // Convert dates to the format expected by datetime-local input
      const taskData = { ...this.task };
      if (taskData.startedDate) {
        taskData.startedDate = this.formatDateForInput(taskData.startedDate);
      }
      if (taskData.completedDate) {
        taskData.completedDate = this.formatDateForInput(
          taskData.completedDate
        );
      }

      // Handle service hierarchy data for pre-selection
      if (
        this.task.serviceItemId &&
        this.task.serviceCategoryName &&
        this.task.serviceSubcategoryName &&
        this.task.serviceItemName
      ) {
        // Create a service item object for the component to pre-select
        this.selectedServiceItem = {
          id: this.task.serviceItemId,
          categoryName: this.task.serviceCategoryName,
          subcategoryName: this.task.serviceSubcategoryName,
          name: this.task.serviceItemName,
          estimatedHours: this.task.estimatedHours || 0,
          description: this.task.description || '',
          categoryId: 0, // Will be set by the service selection component
          subcategoryId: 0, // Will be set by the service selection component
          active: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        };
      } else if (this.task.serviceItemId) {
        // Fallback: Load service item data if only serviceItemId is available
        this.loadServiceItemData(this.task.serviceItemId);
      }

      this.taskForm.patchValue(taskData);

      // Set the service selection component value if we have service data
      if (this.selectedServiceItem) {
        setTimeout(() => {
          this.taskForm
            .get('serviceItemId')
            ?.setValue(this.selectedServiceItem);
        }, 0);
      }

      // Apply role-based field restrictions for staff
      if (this.isStaff && this.isEditMode) {
        this.applyStaffFieldRestrictions();
      }
    }

    this.loadClients();
    this.loadStaff();
  }

  private formatDateForInput(dateString: string): string {
    const date = new Date(dateString);
    return date.toISOString().slice(0, 16);
  }

  private applyStaffFieldRestrictions(): void {
    // Staff can only edit: status, startedDate, completedDate, actualHours
    // All other fields should be read-only

    // Disable fields that staff cannot edit
    this.taskForm.get('title')?.disable();
    this.taskForm.get('description')?.disable();
    this.taskForm.get('clientId')?.disable();
    this.taskForm.get('assignedStaffId')?.disable();
    this.taskForm.get('taskType')?.disable();
    this.taskForm.get('serviceItemId')?.disable();
    this.taskForm.get('priority')?.disable();
    this.taskForm.get('dueDate')?.disable();
    this.taskForm.get('estimatedHours')?.disable();

    // Keep these fields enabled for staff to edit
    // - status (in Assignment Information)
    // - startedDate (in Timeline Information)
    // - completedDate (in Timeline Information)
    // - actualHours (in Time Tracking)
  }

  loadClients(): void {
    this.loadingClients = true;
    this.clientsError = '';
    this.http
      .get<any>(`${API_CONFIG.baseUrl}${API_CONFIG.clients}?size=1000`)
      .subscribe({
        next: (response) => {
          this.availableClients = response.content || response || [];
          this.loadingClients = false;
        },
        error: (error) => {
          console.error('Error loading clients:', error);
          this.availableClients = [];
          this.clientsError = 'Failed to load clients';
          this.loadingClients = false;
        },
      });
  }

  loadStaff(): void {
    this.loadingStaff = true;
    this.staffError = '';
    this.http
      .get<Staff[]>(`${API_CONFIG.baseUrl}${API_CONFIG.tasks}/available-staff`)
      .subscribe({
        next: (staff) => {
          this.availableStaff = staff;
          this.loadingStaff = false;
        },
        error: (error) => {
          console.error('Error loading staff:', error);
          this.availableStaff = [];
          this.staffError = 'Failed to load staff';
          this.loadingStaff = false;
        },
      });
  }

  loadServiceItemData(serviceItemId: number): void {
    this.serviceHierarchyService.getServiceItemById(serviceItemId).subscribe({
      next: (serviceItem) => {
        this.selectedServiceItem = serviceItem;
        // If we're in edit mode and have service data, populate the form
        if (this.isEditMode && serviceItem) {
          this.taskForm.patchValue({
            serviceItemId: serviceItem.id, // Set the full ServiceItem object
          });
        }
      },
      error: (error) => {
        console.error('Error loading service item:', error);
      },
    });
  }

  onSubmit(): void {
    if (this.taskForm.valid) {
      this.loading = true;

      // Get form data, handling disabled controls
      const formData = this.taskForm.getRawValue(); // This includes disabled controls

      // Handle service selection - extract the service item ID
      if (this.selectedServiceItem) {
        formData.serviceItemId = this.selectedServiceItem.id; // Set the full ServiceItem object
        // Set taskType to OTHER for backward compatibility
        formData.taskType = 'OTHER';
      }

      // Convert datetime-local values back to proper format
      if (formData.startedDate) {
        formData.startedDate = new Date(formData.startedDate).toISOString();
      }
      if (formData.completedDate) {
        formData.completedDate = new Date(formData.completedDate).toISOString();
      }

      if (this.isEditMode && this.task) {
        // Update existing task
        this.taskService.updateTask(this.task.id, formData).subscribe({
          next: (updatedTask) => {
            this.loading = false;
            this.saved.emit(updatedTask);
          },
          error: (error) => {
            this.loading = false;
            console.error('Error updating task:', error);
            if (error.error && error.error.details) {
              alert('Error updating task: ' + error.error.details);
            } else {
              alert(
                'Error updating task. Please check the form data and try again.'
              );
            }
          },
        });
      } else {
        // Create new task
        this.taskService.createTask(formData).subscribe({
          next: (newTask) => {
            this.loading = false;
            this.saved.emit(newTask);
          },
          error: (error) => {
            this.loading = false;
            console.error('Error creating task:', error);
            if (error.error && error.error.details) {
              alert('Error creating task: ' + error.error.details);
            } else {
              alert(
                'Error creating task. Please check the form data and try again.'
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
