import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { RouterModule } from '@angular/router';
import {
  ServiceRequestService,
  ServiceRequest,
  ServiceRequestStatistics,
} from '../services/service-request.service';
import { AuthService } from '../services/auth.service';
import { ClientService } from '../services/client.service';
import { StaffService } from '../services/staff.service';
import { ServiceSelectionComponent } from '../shared/service-selection/service-selection.component';
import {
  ServiceItem,
  ServiceHierarchyService,
} from '../services/service-hierarchy.service';
import { PaginationComponent } from '../pagination/pagination.component';
import { ConfirmDialogComponent } from '../components/confirm-dialog.component';
import { DateFormatPipe } from '../shared/pipes/date-format.pipe';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-service-request',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ServiceSelectionComponent,
    PaginationComponent,
    ConfirmDialogComponent,
    DateFormatPipe,
  ],
  templateUrl: './service-request.component.html',
  styleUrls: ['./service-request.component.scss'],
})
export class ServiceRequestComponent implements OnInit {
  serviceRequests: ServiceRequest[] = [];
  statistics: ServiceRequestStatistics | null = null;
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  loading = false;
  error = '';

  // Filters
  searchTerm = '';
  statusFilter = '';
  priorityFilter = '';

  // Form
  serviceRequestForm: FormGroup;
  showAddModal = false;
  showEditModal = false;
  showDetailModal = false;

  // Available clients for admin/staff selection
  availableClients: any[] = [];

  // Available staff for admin assignment
  availableStaff: any[] = [];
  loadingStaff = false;
  staffError = '';

  showAssignModal = false;
  showRejectModal = false;
  showConvertModal = false;

  // Selected items
  selectedServiceRequest: ServiceRequest | null = null;
  selectedServiceItem: ServiceItem | null = null;

  // Delete confirmation
  showDeleteDialog = false;
  serviceRequestToDelete: ServiceRequest | null = null;

  // Cancel confirmation
  showCancelDialog = false;
  serviceRequestToCancel: ServiceRequest | null = null;

  // User role and permissions
  isAdmin = false;
  isStaff = false;
  isClient = false;
  currentUserId: number | null = null;

  // Status and priority options
  statusOptions = [
    { value: '', label: 'All Status' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'ASSIGNED', label: 'Assigned' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'CANCELLED', label: 'Cancelled' },
    { value: 'REJECTED', label: 'Rejected' },
  ];

  statusOptionsForForm = [
    { value: 'PENDING', label: 'Pending' },
    { value: 'ASSIGNED', label: 'Assigned' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'CANCELLED', label: 'Cancelled' },
    { value: 'REJECTED', label: 'Rejected' },
  ];

  priorityOptions = [
    { value: '', label: 'All Priority' },
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' },
  ];

  constructor(
    private serviceRequestService: ServiceRequestService,
    private authService: AuthService,
    private clientService: ClientService,
    private staffService: StaffService,
    private fb: FormBuilder,
    private serviceHierarchyService: ServiceHierarchyService,
    private toastService: ToastService
  ) {
    this.serviceRequestForm = this.fb.group({
      clientId: [null, Validators.required], // Required for admin/staff
      serviceItemId: [null, Validators.required],
      serviceCategoryName: [''],
      serviceSubcategoryName: [''],
      serviceItemName: [''],
      description: ['', Validators.required],
      notes: [''],
      preferredDeadline: ['', Validators.required],
      priority: ['MEDIUM', Validators.required],
      status: ['PENDING', Validators.required],
      assignedStaffId: [null],
      adminNotes: [''],
      staffNotes: [''],
      rejectionReason: [''],
      estimatedPrice: [null],
      finalPrice: [null],
    });
  }

  ngOnInit(): void {
    this.checkUserRole();
    this.loadServiceRequests();
    this.loadStatistics();

    // Load available clients for admin/staff
    if (!this.isClient) {
      this.loadAvailableClients();
    }

    // Load available staff for admin
    if (this.isAdmin) {
      this.loadAvailableStaff();
    }
  }

  checkUserRole(): void {
    const user = this.authService.getUserInfo();
    if (user) {
      this.currentUserId = user.id;
      this.isAdmin = user.role === 'ADMIN';
      this.isStaff = user.role === 'STAFF';
      this.isClient = user.role === 'CLIENT';
    }
  }

  loadServiceRequests(): void {
    this.loading = true;
    this.error = '';

    let request: any;

    if (this.isClient) {
      request = this.serviceRequestService.getMyServiceRequests(
        this.currentPage,
        this.pageSize,
        this.searchTerm,
        this.statusFilter
      );
    } else if (this.isStaff) {
      request = this.serviceRequestService.getMyAssignedServiceRequests(
        this.currentPage,
        this.pageSize,
        this.searchTerm,
        this.statusFilter
      );
    } else {
      request = this.serviceRequestService.getAllServiceRequests(
        this.currentPage,
        this.pageSize,
        this.searchTerm,
        this.statusFilter,
        this.priorityFilter
      );
    }

    request.subscribe({
      next: (response: any) => {
        this.serviceRequests = response.content || [];
        this.totalElements = response.totalElements || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: (error: any) => {
        this.error = 'Failed to load service requests';
        this.loading = false;
        console.error('Error loading service requests:', error);
      },
    });
  }

  loadStatistics(): void {
    if (this.isAdmin) {
      this.serviceRequestService.getServiceRequestStatistics().subscribe({
        next: (stats) => {
          this.statistics = stats;
        },
        error: (error) => {
          console.error('Error loading statistics:', error);
        },
      });
    }
  }

  loadAvailableClients(): void {
    if (this.isAdmin || this.isStaff) {
      this.clientService.getClients(0, 1000).subscribe({
        next: (response: any) => {
          this.availableClients = response.content || [];
        },
        error: (error) => {
          console.error('Error loading clients:', error);
          this.availableClients = [];
        },
      });
    }
  }

  loadAvailableStaff(): void {
    if (this.isAdmin) {
      this.loadingStaff = true;
      this.staffError = '';
      this.staffService.getAllStaff().subscribe({
        next: (staff) => {
          this.availableStaff = staff;
          this.loadingStaff = false;
        },
        error: (error) => {
          console.error('Error loading available staff:', error);
          this.availableStaff = [];
          this.loadingStaff = false;
          this.staffError = 'Failed to load staff members. Please try again.';
        },
      });
    }
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadServiceRequests();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadServiceRequests();
  }

  onPageChange(page: number): void {
    this.currentPage = page - 1;
    this.loadServiceRequests();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadServiceRequests();
  }

  openAddModal(): void {
    this.serviceRequestForm.reset({
      clientId: null,
      serviceItemId: null,
      serviceCategoryName: '',
      serviceSubcategoryName: '',
      serviceItemName: '',
      description: '',
      notes: '',
      preferredDeadline: '',
      priority: 'MEDIUM',
      status: 'PENDING',
      assignedStaffId: null,
      adminNotes: '',
      staffNotes: '',
      rejectionReason: '',
      estimatedPrice: null,
      finalPrice: null,
    });
    this.selectedServiceItem = null;
    this.showAddModal = true;

    // Ensure service selection is enabled for new requests
    this.serviceRequestForm.get('serviceItemId')?.enable();

    // Set clientId validation based on user role
    if (this.isClient) {
      this.serviceRequestForm.get('clientId')?.clearValidators();
      this.serviceRequestForm.get('clientId')?.updateValueAndValidity();
    } else {
      this.serviceRequestForm
        .get('clientId')
        ?.setValidators(Validators.required);
      this.serviceRequestForm.get('clientId')?.updateValueAndValidity();
    }

    // Enable status and staff assignment for admin
    if (this.isAdmin) {
      this.serviceRequestForm.get('status')?.enable();
      this.serviceRequestForm.get('assignedStaffId')?.enable();
    } else if (this.isStaff) {
      // Staff users can edit forms; status and assignment enabled by default here
      this.serviceRequestForm.get('status')?.enable();
      this.serviceRequestForm.get('assignedStaffId')?.enable();
      // Pricing fields allowed for staff
      this.serviceRequestForm.get('estimatedPrice')?.enable();
      this.serviceRequestForm.get('finalPrice')?.enable();
    } else {
      this.serviceRequestForm.get('status')?.disable();
      this.serviceRequestForm.get('assignedStaffId')?.disable();
    }
  }

  openEditModal(serviceRequest: ServiceRequest): void {
    this.selectedServiceRequest = serviceRequest;

    // Handle service hierarchy data for pre-selection
    if (
      serviceRequest.serviceItemId &&
      serviceRequest.serviceCategoryName &&
      serviceRequest.serviceSubcategoryName &&
      serviceRequest.serviceItemName
    ) {
      // Create a service item object for the component to pre-select
      // The service selection component needs categoryId and subcategoryId to properly load the hierarchy
      this.selectedServiceItem = {
        id: serviceRequest.serviceItemId,
        categoryName: serviceRequest.serviceCategoryName,
        subcategoryName: serviceRequest.serviceSubcategoryName,
        name: serviceRequest.serviceItemName,
        estimatedHours: 0,
        description: serviceRequest.description || '',
        categoryId: 0, // Will be set by the service selection component
        subcategoryId: 0, // Will be set by the service selection component
        active: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      // Also set the form value immediately for the service selection
      this.serviceRequestForm.patchValue({
        serviceItemId: serviceRequest.serviceItemId, // Just pass the ID, let the component load the hierarchy
        description: serviceRequest.description,
        notes: serviceRequest.notes,
        preferredDeadline: serviceRequest.preferredDeadline,
        priority: serviceRequest.priority,
        status: serviceRequest.status,
        assignedStaffId: serviceRequest.assignedStaffId,
        clientId: serviceRequest.clientId,
        adminNotes: serviceRequest.adminNotes,
        staffNotes: serviceRequest.staffNotes,
        estimatedPrice: serviceRequest.estimatedPrice,
        finalPrice: serviceRequest.finalPrice,
      });
    } else if (serviceRequest.serviceItemId) {
      // Fallback: Load service item data if only serviceItemId is available
      this.loadServiceItemData(serviceRequest.serviceItemId);
    } else {
      // If no service data, still patch the other form values
      this.serviceRequestForm.patchValue({
        description: serviceRequest.description,
        notes: serviceRequest.notes,
        preferredDeadline: serviceRequest.preferredDeadline,
        priority: serviceRequest.priority,
        status: serviceRequest.status,
        assignedStaffId: serviceRequest.assignedStaffId,
        clientId: serviceRequest.clientId,
        adminNotes: serviceRequest.adminNotes,
        staffNotes: serviceRequest.staffNotes,
        estimatedPrice: serviceRequest.estimatedPrice,
        finalPrice: serviceRequest.finalPrice,
      });
    }

    // Disable service selection for clients when editing
    if (this.isClient) {
      this.serviceRequestForm.get('serviceItemId')?.disable();
    }

    // Handle status and staff assignment based on current status and user role
    if (this.isAdmin) {
      if (serviceRequest.status === 'CANCELLED') {
        // For cancelled services, admin can only view details
        this.serviceRequestForm.disable();
      } else {
        // For active services, admin can edit everything
        this.serviceRequestForm.enable();
        this.serviceRequestForm.get('status')?.enable();
        this.serviceRequestForm.get('assignedStaffId')?.enable();
      }
    } else if (this.isStaff) {
      if (
        serviceRequest.status === 'CANCELLED' ||
        serviceRequest.status === 'REJECTED'
      ) {
        this.serviceRequestForm.disable();
      } else if (this.isStaff) {
        // Staff assigned to this request can edit everything
        this.serviceRequestForm.enable();
        this.serviceRequestForm.get('status')?.enable();
        this.serviceRequestForm.get('assignedStaffId')?.enable();
        this.serviceRequestForm.get('description')?.enable();
        this.serviceRequestForm.get('notes')?.enable();
        this.serviceRequestForm.get('preferredDeadline')?.enable();
        this.serviceRequestForm.get('priority')?.enable();
        this.serviceRequestForm.get('adminNotes')?.disable();
        this.serviceRequestForm.get('staffNotes')?.enable();
        this.serviceRequestForm.get('estimatedPrice')?.enable();
      } else {
        // Staff cannot edit requests not assigned to them
        this.serviceRequestForm.disable();
      }
    } else {
      // Non-admin/non-staff (clients) have limited editing capabilities managed elsewhere
      this.serviceRequestForm.get('status')?.disable();
      this.serviceRequestForm.get('assignedStaffId')?.disable();
    }

    this.showEditModal = true;

    // Set the service selection component value immediately after patchValue
    // Use setTimeout with 0 delay to ensure the component is fully rendered
    if (this.selectedServiceItem) {
      setTimeout(() => {
        this.serviceRequestForm
          .get('serviceItemId')
          ?.setValue(serviceRequest.serviceItemId); // Just pass the ID
      }, 0);
    }
  }

  openDetailModal(serviceRequest: ServiceRequest): void {
    this.selectedServiceRequest = serviceRequest;
    this.showDetailModal = true;
  }

  openAssignModal(serviceRequest: ServiceRequest): void {
    this.selectedServiceRequest = serviceRequest;
    this.showAssignModal = true;
  }

  openRejectModal(serviceRequest: ServiceRequest): void {
    this.selectedServiceRequest = serviceRequest;
    this.showRejectModal = true;
  }

  openConvertModal(serviceRequest: ServiceRequest): void {
    this.selectedServiceRequest = serviceRequest;
    this.showConvertModal = true;

    // Auto-populate task fields based on service request data
    // Use setTimeout to ensure DOM elements are rendered before populating
    setTimeout(() => {
      this.autoPopulateTaskFields(serviceRequest);
    }, 100);

    // Also try again after a longer delay as a fallback
    setTimeout(() => {
      this.autoPopulateTaskFields(serviceRequest);
    }, 500);
  }

  onServiceSelected(serviceItem: ServiceItem): void {
    // Update the selected service item with complete data
    this.selectedServiceItem = {
      id: serviceItem.id,
      categoryName: serviceItem.categoryName,
      subcategoryName: serviceItem.subcategoryName,
      name: serviceItem.name,
      estimatedHours: serviceItem.estimatedHours || 0,
      description: serviceItem.description || '',
      categoryId: serviceItem.categoryId || 0,
      subcategoryId: serviceItem.subcategoryId || 0,
      active: serviceItem.active || true,
      createdAt: serviceItem.createdAt || new Date().toISOString(),
      updatedAt: serviceItem.updatedAt || new Date().toISOString(),
    };

    // Update the form with the new service data
    this.serviceRequestForm.patchValue({
      serviceItemId: serviceItem.id,
      serviceCategoryName: serviceItem.categoryName,
      serviceSubcategoryName: serviceItem.subcategoryName,
      serviceItemName: serviceItem.name,
    });
  }

  loadServiceItemData(serviceItemId: number): void {
    this.serviceHierarchyService.getServiceItemById(serviceItemId).subscribe({
      next: (serviceItem) => {
        this.selectedServiceItem = serviceItem;
        // If we're in edit mode and have service data, populate the form
        if (this.showEditModal && serviceItem) {
          setTimeout(() => {
            this.serviceRequestForm
              .get('serviceItemId')
              ?.setValue(serviceItemId); // Just pass the ID
          }, 0);
        }
      },
      error: (error) => {
        console.error('Error loading service item:', error);
      },
    });
  }

  onSubmit(): void {
    if (this.serviceRequestForm.valid) {
      // Use getRawValue() to include disabled controls (like serviceItemId for clients)
      const formData = this.serviceRequestForm.getRawValue();

      // Handle service selection - extract the service item data
      // Always prioritize the form values as they reflect the current selection
      if (formData.serviceItemId) {
        // If we have a selectedServiceItem that matches the form's serviceItemId, use it
        if (
          this.selectedServiceItem &&
          this.selectedServiceItem.id === formData.serviceItemId
        ) {
          formData.serviceItemId = this.selectedServiceItem.id;
          formData.serviceCategoryName = this.selectedServiceItem.categoryName;
          formData.serviceSubcategoryName =
            this.selectedServiceItem.subcategoryName;
          formData.serviceItemName = this.selectedServiceItem.name;
        } else {
          // If no matching selectedServiceItem, we need to fetch the service item data
          // This ensures we have the complete service hierarchy information
          this.serviceHierarchyService
            .getServiceItemById(formData.serviceItemId)
            .subscribe({
              next: (serviceItem) => {
                if (serviceItem) {
                  formData.serviceItemId = serviceItem.id;
                  formData.serviceCategoryName = serviceItem.categoryName;
                  formData.serviceSubcategoryName = serviceItem.subcategoryName;
                  formData.serviceItemName = serviceItem.name;

                  // Now submit the form with complete service data
                  this.submitFormWithServiceData(formData);
                }
              },
              error: (error) => {
                console.error('Error fetching service item data:', error);
                // Submit anyway with available data
                this.submitFormWithServiceData(formData);
              },
            });
          return; // Exit early, will submit after fetching service data
        }
      }

      // Handle status and staff assignment for roles
      if (this.isAdmin) {
        // Admin can set status and assign staff
        if (formData.status === 'CANCELLED') {
          // If status is being set to cancelled, clear staff assignment
          formData.assignedStaffId = null;
        }
      } else if (this.isStaff) {
        // Staff can update status and keep assignment (no override)
        // No changes here; allow status and assignedStaffId to pass through
      } else {
        // Clients cannot change status or assign staff
        formData.status = undefined;
        formData.assignedStaffId = undefined;
      }

      // For clients, set their own clientId
      if (this.isClient) {
        const user = this.authService.getUserInfo();
        if (user) {
          formData.clientId = user.id;
        }
      }

      // Do not strip staff fields; allow staff to send pricing/adminNotes
      // Normalize numeric fields before sending in submitFormWithServiceData

      // Submit the form with complete service data
      this.submitFormWithServiceData(formData);
    }
  }

  submitFormWithServiceData(formData: any): void {
    // Normalize types and defaults
    const normalizeNumber = (v: any) => {
      if (v === '' || v === null || v === undefined) return null;
      const num = typeof v === 'string' ? v.trim() : v;
      const parsed = parseFloat(num);
      return isNaN(parsed) ? null : parsed;
    };

    const normalizeId = (v: any) => {
      if (v === '' || v === null || v === undefined) return null;
      const parsed = parseInt(v, 10);
      return isNaN(parsed) ? null : parsed;
    };

    const formDataToSend = {
      ...formData,
      clientId: normalizeId(formData.clientId),
      serviceItemId: normalizeId(formData.serviceItemId),
      assignedStaffId:
        formData.assignedStaffId !== undefined
          ? normalizeId(formData.assignedStaffId)
          : undefined,
      adminNotes: formData.adminNotes !== undefined ? formData.adminNotes : '',
      staffNotes: formData.staffNotes !== undefined ? formData.staffNotes : '',
      rejectionReason:
        formData.rejectionReason !== undefined ? formData.rejectionReason : '',
      estimatedPrice:
        formData.estimatedPrice !== undefined
          ? normalizeNumber(formData.estimatedPrice)
          : null,
      finalPrice:
        formData.finalPrice !== undefined
          ? normalizeNumber(formData.finalPrice)
          : null,
    };

    if (this.showAddModal) {
      this.serviceRequestService
        .createServiceRequest(formDataToSend)
        .subscribe({
          next: () => {
            this.showAddModal = false;
            this.loadServiceRequests();
            this.loadStatistics();
            this.toastService.showSuccess('Service request created');
          },
          error: (error) => {
            console.error('Error creating service request:', error);
            this.toastService.showError('Failed to create service request');
          },
        });
    } else if (this.showEditModal && this.selectedServiceRequest) {
      this.serviceRequestService
        .updateServiceRequest(this.selectedServiceRequest.id!, formDataToSend)
        .subscribe({
          next: () => {
            this.showEditModal = false;
            this.loadServiceRequests();
            this.loadStatistics();

            // Refresh the selected service request data to ensure it's up to date
            this.refreshSelectedServiceRequest(formDataToSend.serviceItemId);
            this.toastService.showSuccess('Service request updated');
          },
          error: (error) => {
            console.error('Error updating service request:', error);
            this.toastService.showError('Failed to update service request');
          },
        });
    }
  }

  refreshSelectedServiceRequest(serviceItemId: number): void {
    // Refresh the selected service request data to ensure it shows the updated service hierarchy
    if (this.selectedServiceRequest) {
      this.serviceRequestService
        .getServiceRequestById(this.selectedServiceRequest.id!)
        .subscribe({
          next: (updatedServiceRequest) => {
            if (updatedServiceRequest) {
              // Update the selected service request with fresh data
              this.selectedServiceRequest = updatedServiceRequest;
            }
          },
          error: (error) => {
            console.error('Error refreshing service request data:', error);
          },
        });
    }
  }

  onAssign(staffId: number, adminNotes?: string): void {
    if (this.selectedServiceRequest) {
      this.serviceRequestService
        .assignServiceRequestToStaff(
          this.selectedServiceRequest.id!,
          staffId,
          adminNotes
        )
        .subscribe({
          next: () => {
            this.showAssignModal = false;
            this.loadServiceRequests();
            this.toastService.showSuccess('Service request assigned');
          },
          error: (error) => {
            console.error('Error assigning service request:', error);
            this.toastService.showError('Failed to assign service request');
          },
        });
    }
  }

  onReject(rejectionReason: string): void {
    if (this.selectedServiceRequest) {
      this.serviceRequestService
        .rejectServiceRequest(this.selectedServiceRequest.id!, rejectionReason)
        .subscribe({
          next: () => {
            this.showRejectModal = false;
            this.loadServiceRequests();
            this.toastService.showSuccess('Service request rejected');
          },
          error: (error) => {
            console.error('Error rejecting service request:', error);
            this.toastService.showError('Failed to reject service request');
          },
        });
    }
  }

  onConvertToTask(
    taskTitle: string,
    taskDescription: string,
    taskPriority: string,
    taskDueDate: string,
    taskEstimatedHours: number,
    taskAssignedStaffId?: string | number | null
  ): void {
    if (this.selectedServiceRequest) {
      // Prepare task data
      const taskData = {
        title: taskTitle,
        description: taskDescription,
        priority: taskPriority,
        dueDate: taskDueDate,
        estimatedHours: taskEstimatedHours || 0,
        assignedStaffId:
          taskAssignedStaffId || (this.isStaff ? this.currentUserId : null),
      };

      this.serviceRequestService
        .convertServiceRequestToTask(this.selectedServiceRequest.id!, taskData)
        .subscribe({
          next: () => {
            this.showConvertModal = false;
            this.loadServiceRequests();
            this.toastService.showSuccess('Converted to task');
          },
          error: (error) => {
            console.error('Error converting service request to task:', error);
            this.toastService.showError('Failed to convert to task');
          },
        });
    }
  }

  closeModal(): void {
    this.showAddModal = false;
    this.showEditModal = false;
    this.showDetailModal = false;
    this.showAssignModal = false;
    this.showRejectModal = false;
    this.showConvertModal = false;
    this.showDeleteDialog = false;
    this.showCancelDialog = false;
    this.selectedServiceRequest = null;
    this.selectedServiceItem = null;
    this.serviceRequestToDelete = null;
    this.serviceRequestToCancel = null;

    // Re-enable all form controls when closing
    this.serviceRequestForm.enable();

    // Reset the form to clear any previous values
    this.serviceRequestForm.reset({
      priority: 'MEDIUM',
      status: 'PENDING',
      assignedStaffId: null,
      adminNotes: '',
      staffNotes: '',
      rejectionReason: '',
      estimatedPrice: null,
      finalPrice: null,
    });

    // Reset validation for clientId
    if (this.isClient) {
      this.serviceRequestForm.get('clientId')?.clearValidators();
      this.serviceRequestForm.get('clientId')?.updateValueAndValidity();
    } else {
      this.serviceRequestForm
        .get('clientId')
        ?.setValidators(Validators.required);
      this.serviceRequestForm.get('clientId')?.updateValueAndValidity();
    }

    // Reset status and staff assignment validators
    if (this.isAdmin) {
      this.serviceRequestForm.get('status')?.enable();
      this.serviceRequestForm.get('assignedStaffId')?.enable();
    } else if (this.isStaff) {
      // Staff users can edit their own requests but not change status or assign staff
      this.serviceRequestForm.get('status')?.disable();
      this.serviceRequestForm.get('assignedStaffId')?.disable();
      // Staff users cannot edit pricing fields
      this.serviceRequestForm.get('estimatedPrice')?.disable();
      this.serviceRequestForm.get('finalPrice')?.disable();
    } else {
      this.serviceRequestForm.get('status')?.disable();
      this.serviceRequestForm.get('assignedStaffId')?.disable();
    }
  }

  getStatusBadgeClass(status: string): string {
    return this.serviceRequestService.getStatusBadgeClass(status);
  }

  getPriorityBadgeClass(priority: string): string {
    return this.serviceRequestService.getPriorityBadgeClass(priority);
  }

  getStatusDisplayName(status: string): string {
    return this.serviceRequestService.getStatusDisplayName(status);
  }

  getPriorityDisplayName(priority: string): string {
    return this.serviceRequestService.getPriorityDisplayName(priority);
  }

  getCurrentStaffName(): string {
    const user = this.authService.getUserInfo();
    if (user) {
      return `${user.firstName} ${user.lastName}`;
    }
    return 'Current Staff';
  }

  autoPopulateTaskFields(serviceRequest: ServiceRequest): void {
    // Validate service request data
    if (!serviceRequest) {
      console.error('No service request provided for auto-population');
      return;
    }

    // Auto-populate task title and description from service request
    if (serviceRequest.serviceItemName) {
      // Set task title to service item name
      const taskTitleElement = document.querySelector(
        '#taskTitle'
      ) as HTMLInputElement;
      if (taskTitleElement) {
        taskTitleElement.value = serviceRequest.serviceItemName;
      } else {
      }
    } else {
    }

    // Auto-populate due date from preferred deadline
    if (serviceRequest.preferredDeadline) {
      const taskDueDateElement = document.querySelector(
        '#taskDueDate'
      ) as HTMLInputElement;
      if (taskDueDateElement) {
        try {
          // Convert the date string to YYYY-MM-DD format for the date input
          const dueDate = new Date(serviceRequest.preferredDeadline);

          // Check if the date is valid
          if (isNaN(dueDate.getTime())) {
            return;
          }

          const formattedDate = dueDate.toISOString().split('T')[0];
          taskDueDateElement.value = formattedDate;
        } catch (error) {
          console.error('Error formatting due date:', error);
        }
      }
    }

    // Auto-populate priority from service request priority
    if (serviceRequest.priority) {
      const taskPriorityElement = document.querySelector(
        '#taskPriority'
      ) as HTMLSelectElement;
      if (taskPriorityElement) {
        taskPriorityElement.value = serviceRequest.priority;
      }
    }

    // Auto-populate estimated hours if available (we'll need to fetch service item details)
    if (serviceRequest.serviceItemId) {
      this.loadServiceItemForTaskConversion(serviceRequest.serviceItemId);
    }
  }

  loadServiceItemForTaskConversion(serviceItemId: number): void {
    this.serviceHierarchyService.getServiceItemById(serviceItemId).subscribe({
      next: (serviceItem) => {
        if (serviceItem) {
          // Set task description from service item description (same logic as new task creation)
          const taskDescriptionElement = document.querySelector(
            '#taskDescription'
          ) as HTMLTextAreaElement;
          if (taskDescriptionElement) {
            const description =
              serviceItem.description || `Service: ${serviceItem.name}`;
            taskDescriptionElement.value = description;
          }

          // Set estimated hours if available
          if (serviceItem.estimatedHours > 0) {
            const taskEstimatedHoursElement = document.querySelector(
              '#taskEstimatedHours'
            ) as HTMLInputElement;
            if (taskEstimatedHoursElement) {
              taskEstimatedHoursElement.value =
                serviceItem.estimatedHours.toString();
            }
          }
        }
      },
      error: (error) => {
        console.error('Error loading service item for task conversion:', error);
      },
    });
  }

  // Manual trigger for auto-population (useful for debugging)
  manualTriggerAutoPopulate(): void {
    if (this.selectedServiceRequest) {
      this.autoPopulateTaskFields(this.selectedServiceRequest);
    }
  }

  convertToTask(): void {
    // Get values from template reference variables
    const taskTitle = (document.querySelector('#taskTitle') as HTMLInputElement)
      ?.value;
    const taskDescription = (
      document.querySelector('#taskDescription') as HTMLTextAreaElement
    )?.value;
    const taskPriority = (
      document.querySelector('#taskPriority') as HTMLSelectElement
    )?.value;
    const taskDueDate = (
      document.querySelector('#taskDueDate') as HTMLInputElement
    )?.value;
    const taskEstimatedHours = (
      document.querySelector('#taskEstimatedHours') as HTMLInputElement
    )?.value;

    let taskAssignedStaffId: string | number | null = null;
    if (this.isAdmin) {
      const taskAssignedStaff = document.querySelector(
        '#taskAssignedStaff'
      ) as HTMLSelectElement;
      taskAssignedStaffId = taskAssignedStaff?.value || null;
    }

    // Validate required fields
    if (!taskTitle?.trim()) {
      alert('Task title is required. Please enter a title for the task.');
      return;
    }

    if (!taskDescription?.trim()) {
      alert(
        'Task description is required. Please enter a description for the task.'
      );
      return;
    }

    // Use fallback values if auto-population didn't work
    const finalTaskTitle = taskTitle.trim();
    const finalTaskDescription = taskDescription.trim();
    const finalTaskPriority = taskPriority || 'MEDIUM';
    const finalTaskDueDate = taskDueDate || this.getDefaultDueDate();
    const finalTaskEstimatedHours = parseFloat(taskEstimatedHours) || 0;

    this.onConvertToTask(
      finalTaskTitle,
      finalTaskDescription,
      finalTaskPriority,
      finalTaskDueDate,
      finalTaskEstimatedHours,
      taskAssignedStaffId
    );
  }

  getDefaultDueDate(): string {
    // Set default due date to 30 days from now if no preferred deadline
    const defaultDate = new Date();
    defaultDate.setDate(defaultDate.getDate() + 30);
    return defaultDate.toISOString().split('T')[0];
  }

  canEdit(serviceRequest: ServiceRequest): boolean {
    // For rejected or cancelled services, only admin can edit
    if (
      serviceRequest.status === 'REJECTED' ||
      serviceRequest.status === 'CANCELLED'
    ) {
      return this.isAdmin;
    }

    // Admins can edit any non-cancelled request
    if (this.isAdmin) {
      return true;
    }

    // Clients can edit their own pending or assigned requests
    if (this.isClient) {
      return (
        serviceRequest.status === 'PENDING' ||
        serviceRequest.status === 'ASSIGNED'
      );
    }

    // Staff can edit requests assigned to them (including reject and convert to task)
    if (this.isStaff) {
      return (
        serviceRequest.assignedStaffId === this.currentUserId &&
        (serviceRequest.status === 'PENDING' ||
          serviceRequest.status === 'ASSIGNED' ||
          serviceRequest.status === 'IN_PROGRESS' ||
          serviceRequest.status === 'COMPLETED')
      );
    }

    return false;
  }

  canDelete(serviceRequest: ServiceRequest): boolean {
    // Only admin can delete, and only for rejected or cancelled services
    if (this.isAdmin) {
      return true;
    }
    return false;
  }

  canAssign(serviceRequest: ServiceRequest): boolean {
    // Admins can assign staff to pending or assigned requests
    if (this.isAdmin) {
      return (
        serviceRequest.status === 'PENDING' ||
        serviceRequest.status === 'ASSIGNED'
      );
    }

    // For other users, use the existing logic
    return serviceRequest.canAssign || false;
  }

  canReject(serviceRequest: ServiceRequest): boolean {
    // Admins can reject pending requests
    if (this.isAdmin) {
      return serviceRequest.status === 'PENDING';
    }

    // Staff can reject their own assigned requests
    if (this.isStaff) {
      return (
        serviceRequest.assignedStaffId === this.currentUserId &&
        (serviceRequest.status === 'ASSIGNED' ||
          serviceRequest.status === 'IN_PROGRESS')
      );
    }

    // For other users, use the existing logic
    return serviceRequest.canReject || false;
  }

  canConvertToTask(serviceRequest: ServiceRequest): boolean {
    // Admin can convert any service request to task
    if (this.isAdmin) {
      return (
        serviceRequest.status === 'ASSIGNED' ||
        serviceRequest.status === 'IN_PROGRESS' ||
        serviceRequest.status === 'COMPLETED'
      );
    }

    // Staff can convert their own assigned service requests to task
    if (this.isStaff) {
      return (
        serviceRequest.assignedStaffId === this.currentUserId &&
        (serviceRequest.status === 'ASSIGNED' ||
          serviceRequest.status === 'IN_PROGRESS' ||
          serviceRequest.status === 'COMPLETED')
      );
    }

    // For other users, use the existing logic
    return serviceRequest.canConvertToTask || false;
  }

  canCancel(serviceRequest: ServiceRequest): boolean {
    // Admins can cancel any pending or assigned request
    if (this.isAdmin) {
      return (
        serviceRequest.status === 'PENDING' ||
        serviceRequest.status === 'ASSIGNED'
      );
    }

    // Clients can cancel their own pending or assigned requests
    return (
      this.isClient &&
      (serviceRequest.status === 'PENDING' ||
        serviceRequest.status === 'ASSIGNED')
    );
  }

  // Delete methods
  deleteServiceRequest(serviceRequest: ServiceRequest): void {
    if (!this.canDelete(serviceRequest)) {
      return;
    }
    this.serviceRequestToDelete = serviceRequest;
    this.showDeleteDialog = true;
  }

  confirmDelete(): void {
    if (this.serviceRequestToDelete) {
      this.serviceRequestService
        .deleteServiceRequest(this.serviceRequestToDelete.id!)
        .subscribe({
          next: () => {
            this.showDeleteDialog = false;
            this.serviceRequestToDelete = null;
            this.loadServiceRequests();
            this.toastService.showSuccess('Service request deleted');
          },
          error: (error) => {
            console.error('Error deleting service request:', error);
            this.showDeleteDialog = false;
            this.serviceRequestToDelete = null;
            this.toastService.showError('Failed to delete service request');
          },
        });
    }
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
    this.serviceRequestToDelete = null;
  }

  // Cancel methods
  cancelServiceRequest(serviceRequest: ServiceRequest): void {
    if (!this.canCancel(serviceRequest)) {
      return;
    }
    this.serviceRequestToCancel = serviceRequest;
    this.showCancelDialog = true;
  }

  confirmCancel(): void {
    if (this.serviceRequestToCancel) {
      this.serviceRequestService
        .updateServiceRequestStatus(
          this.serviceRequestToCancel.id!,
          'CANCELLED'
        )
        .subscribe({
          next: () => {
            this.showCancelDialog = false;
            this.serviceRequestToCancel = null;
            this.loadServiceRequests();
            this.loadStatistics();
            this.toastService.showSuccess('Service request cancelled');
          },
          error: (error) => {
            console.error('Error cancelling service request:', error);
            this.showCancelDialog = false;
            this.serviceRequestToCancel = null;
            this.toastService.showError('Failed to cancel service request');
          },
        });
    }
  }

  cancelCancelDialog(): void {
    this.showCancelDialog = false;
    this.serviceRequestToCancel = null;
  }

  // New method to auto-open convert modal when status changes to COMPLETED
  onStatusChange(serviceRequest: ServiceRequest, newStatus: string): void {
    if (newStatus === 'COMPLETED') {
      // Auto-open convert to task modal
      this.selectedServiceRequest = serviceRequest;
      this.showConvertModal = true;

      // Auto-populate the task fields
      setTimeout(() => {
        this.autoPopulateTaskFields(serviceRequest);
      }, 100);
    }
  }

  // Method to handle status change from form dropdown
  onStatusChangeFromForm(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const newStatus = target.value;

    if (this.selectedServiceRequest && newStatus === 'COMPLETED') {
      // Auto-open convert to task modal
      this.showConvertModal = true;

      // Auto-populate the task fields
      setTimeout(() => {
        this.autoPopulateTaskFields(this.selectedServiceRequest!);
      }, 100);
    }
  }
}
