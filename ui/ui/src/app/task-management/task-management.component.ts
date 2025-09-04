import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import {
  TaskService,
  Task,
  TaskStatistics,
  StaffWorkload,
} from '../services/task.service';
import { PaginationComponent } from '../pagination/pagination.component';
import { ConfirmDialogComponent } from '../components/confirm-dialog.component';
import { TaskFormModalComponent } from './task-form-modal.component';
import { ToastService } from '../services/toast.service';
import { AuthService } from '../services/auth.service';
import { ClientService } from '../services/client.service';
import { StaffService } from '../services/staff.service';
import { ServiceHierarchyService } from '../services/service-hierarchy.service';
import {
  StaffDashboardService,
  StaffDashboardStats,
} from '../services/staff-dashboard.service';
import { DateFormatPipe } from '../shared/pipes/date-format.pipe';

@Component({
  selector: 'app-task-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PaginationComponent,
    ConfirmDialogComponent,
    TaskFormModalComponent,
    DateFormatPipe,
  ],
  templateUrl: './task-management.component.html',
  styleUrls: ['./task-management.component.scss'],
})
export class TaskManagementComponent implements OnInit {
  // Data
  tasks: any[] = [];
  statistics: TaskStatistics | null = null;
  workloadData: StaffWorkload[] = [];
  availableStaff: StaffWorkload[] = [];

  // Pagination
  currentPage = 0;
  pageSize = 5;
  totalItems = 0;

  // Loading and error states
  loading = false;
  error = '';

  // Search and filters
  searchTerm = '';
  selectedStatus = '';
  selectedPriority = '';

  // Service hierarchy filters
  selectedServiceCategory = '';
  selectedServiceSubcategory = '';
  selectedServiceItem = '';

  // Service hierarchy data for filters
  serviceCategories: any[] = [];
  serviceSubcategories: any[] = [];
  serviceItems: any[] = [];

  // Modals
  showTaskModal = false;
  showTaskDetailModal = false;
  showViewTaskModal = false;
  showReassignModal = false;
  showStatisticsModal = false;
  showWorkloadModal = false;
  showSendEmailModal = false;
  isEditMode = false;

  // Selected task for operations
  selectedTask: Task | null = null;
  selectedTaskForView: Task | null = null;
  selectedTaskForEmail: Task | null = null;
  newStaffId: number | null = null;
  taskToDelete: Task | null = null;
  showDeleteDialog = false;

  // Client-specific properties
  isClient = false;
  isStaff = false;
  currentUser: any = null;
  currentStaffId: number | null = null;
  showAcknowledgeModal = false;
  acknowledgeMessage = '';
  selectedTaskForAcknowledge: Task | null = null;

  // Task detail
  taskDetail: any = null;

  // Options for dropdowns
  statuses = [
    { value: 'PENDING', label: 'Pending' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'ON_HOLD', label: 'On Hold' },
    { value: 'CANCELLED', label: 'Cancelled' },
  ];

  priorities = [
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' },
  ];

  constructor(
    private taskService: TaskService,
    private fb: FormBuilder,
    private toastService: ToastService,
    private authService: AuthService,
    private clientService: ClientService,
    private staffService: StaffService,
    private staffDashboardService: StaffDashboardService,
    private serviceHierarchyService: ServiceHierarchyService
  ) {
    // Initialize form will be handled by the task form modal component
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.isClient = this.authService.isClient();
    this.isStaff = this.authService.isStaff();

    // Load service hierarchy data for filters
    this.loadServiceCategories();

    if (this.isClient) {
      this.loadClientTasks();
      this.loadClientStatistics();
    } else if (this.isStaff) {
      this.loadCurrentStaffId();
    } else {
      this.loadTasks();
      this.loadStatistics();
      this.loadAvailableStaff();
      this.loadWorkloadData();
    }
  }

  loadCurrentStaffId(): void {
    this.staffService.getCurrentStaff().subscribe({
      next: (staff) => {
        this.currentStaffId = staff.id;
        this.loadStaffTasks();
        this.loadStaffStatistics();
      },
      error: (error) => {
        console.error('Error loading current staff:', error);
        this.toastService.showError('Failed to load staff information');
      },
    });
  }

  loadStaffTasks(): void {
    if (!this.currentStaffId) return;

    this.loading = true;
    this.error = '';

    const filters: any = {
      page: this.currentPage,
      size: this.pageSize,
      assignedStaffId: this.currentStaffId.toString(),
    };

    // Include search term in filters if it exists
    if (this.searchTerm.trim()) {
      filters.searchTerm = this.searchTerm.trim();
    }

    // Include other filter parameters
    if (this.selectedStatus) filters.status = this.selectedStatus;
    if (this.selectedPriority) filters.priority = this.selectedPriority;

    // Include service hierarchy filters
    if (this.selectedServiceCategory)
      filters.serviceCategoryId = this.selectedServiceCategory;
    if (this.selectedServiceSubcategory)
      filters.serviceSubcategoryId = this.selectedServiceSubcategory;
    if (this.selectedServiceItem)
      filters.serviceItemId = this.selectedServiceItem;

    this.taskService.getTasks(filters).subscribe({
      next: (response) => {
        this.tasks = response.content || [];
        this.totalItems = response.totalElements || 0;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load your tasks';
        this.loading = false;
        console.error('Error loading staff tasks:', error);
      },
    });
  }

  loadStaffStatistics(): void {
    if (!this.currentStaffId) return;

    // Get staff dashboard stats which include task statistics for the current staff
    this.staffDashboardService.getDashboardStats().subscribe({
      next: (stats: StaffDashboardStats) => {
        this.statistics = {
          totalTasks: stats.totalAssignedTasks,
          pendingTasks: stats.pendingTasks,
          inProgressTasks: stats.inProgressTasks,
          completedTasks: stats.completedTasks,
          onHoldTasks: stats.onHoldTasks,
          cancelledTasks: stats.cancelledTasks,
          overdueTasks: stats.overdueTasks,
        };
      },
      error: (error: any) => {
        console.error('Error loading staff statistics:', error);
        this.statistics = {
          totalTasks: 0,
          pendingTasks: 0,
          inProgressTasks: 0,
          completedTasks: 0,
          onHoldTasks: 0,
          cancelledTasks: 0,
          overdueTasks: 0,
        };
      },
    });
  }

  loadTasks(): void {
    this.loading = true;
    this.error = '';

    const filters: any = {
      page: this.currentPage,
      size: this.pageSize,
    };

    // Include search term in filters if it exists
    if (this.searchTerm.trim()) {
      filters.searchTerm = this.searchTerm.trim();
    }

    // Include other filter parameters
    if (this.selectedStatus) filters.status = this.selectedStatus;
    if (this.selectedPriority) filters.priority = this.selectedPriority;

    // Include service hierarchy filters
    if (this.selectedServiceCategory)
      filters.serviceCategoryId = this.selectedServiceCategory;
    if (this.selectedServiceSubcategory)
      filters.serviceSubcategoryId = this.selectedServiceSubcategory;
    if (this.selectedServiceItem)
      filters.serviceItemId = this.selectedServiceItem;

    this.taskService.getTasks(filters).subscribe({
      next: (response) => {
        this.tasks = response.content || [];
        this.totalItems = response.totalElements || 0;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load tasks';
        this.loading = false;
        console.error('Error loading tasks:', error);
      },
    });
  }

  loadClientTasks(): void {
    this.loading = true;
    this.error = '';

    const filters: any = {
      page: this.currentPage,
      size: this.pageSize,
    };

    // Add search term if provided
    if (this.searchTerm.trim()) {
      filters.searchTerm = this.searchTerm.trim();
    }

    // Include other filter parameters
    if (this.selectedStatus) filters.status = this.selectedStatus;
    if (this.selectedPriority) filters.priority = this.selectedPriority;

    // Include service hierarchy filters
    if (this.selectedServiceCategory)
      filters.serviceCategoryId = this.selectedServiceCategory;
    if (this.selectedServiceSubcategory)
      filters.serviceSubcategoryId = this.selectedServiceSubcategory;
    if (this.selectedServiceItem)
      filters.serviceItemId = this.selectedServiceItem;

    this.clientService.getMyTasksWithFilters(filters).subscribe({
      next: (response) => {
        this.tasks = response.content || [];
        this.totalItems = response.totalElements || 0;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load your tasks';
        this.loading = false;
        console.error('Error loading client tasks:', error);
      },
    });
  }

  searchTasks(): void {
    this.currentPage = 0; // Reset to first page when searching
    if (this.isStaff) {
      this.loadStaffTasks();
    } else if (this.isClient) {
      this.loadClientTasks();
    } else {
      this.loadTasks();
    }
  }

  loadStatistics(): void {
    this.taskService.getTaskStatistics().subscribe({
      next: (statistics) => {
        this.statistics = statistics;
      },
      error: (error) => {
        console.error('Error loading statistics:', error);
      },
    });
  }

  loadClientStatistics(): void {
    this.clientService.getDashboardStats().subscribe({
      next: (stats) => {
        // Convert client stats to task statistics format
        this.statistics = {
          totalTasks: stats.totalTasks,
          pendingTasks: stats.pendingTasks,
          inProgressTasks: 0, // Not available in client stats
          completedTasks: stats.completedTasks,
          onHoldTasks: 0, // Not available in client stats
          cancelledTasks: 0, // Not available in client stats
          overdueTasks: stats.overdueTasks,
        };
      },
      error: (error) => {
        console.error('Error loading client statistics:', error);
      },
    });
  }

  loadAvailableStaff(): void {
    this.taskService.getAvailableStaffForAssignment().subscribe({
      next: (staff) => {
        this.availableStaff = staff;
      },
      error: (error) => {
        console.error('Error loading available staff:', error);
      },
    });
  }

  loadWorkloadData(): void {
    this.taskService.getStaffWorkloadSummary().subscribe({
      next: (workload) => {
        this.workloadData = workload;
      },
      error: (error) => {
        console.error('Error loading workload data:', error);
      },
    });
  }

  // Service hierarchy filter methods
  loadServiceCategories(): void {
    this.serviceHierarchyService.getAllCategories().subscribe({
      next: (categories) => {
        this.serviceCategories = categories.filter((cat) => cat.active);
      },
      error: (error) => {
        console.error('Error loading service categories:', error);
      },
    });
  }

  onServiceCategoryChange(): void {
    this.selectedServiceSubcategory = '';
    this.selectedServiceItem = '';
    this.serviceSubcategories = [];
    this.serviceItems = [];

    if (this.selectedServiceCategory) {
      this.loadServiceSubcategories(this.selectedServiceCategory);
    }

    this.applyFilters();
  }

  loadServiceSubcategories(categoryId: string): void {
    this.serviceHierarchyService
      .getSubcategoriesByCategory(+categoryId)
      .subscribe({
        next: (subcategories) => {
          this.serviceSubcategories = subcategories.filter((sub) => sub.active);
        },
        error: (error) => {
          console.error('Error loading service subcategories:', error);
        },
      });
  }

  onServiceSubcategoryChange(): void {
    this.selectedServiceItem = '';
    this.serviceItems = [];

    if (this.selectedServiceSubcategory) {
      this.loadServiceItems(this.selectedServiceSubcategory);
    }

    this.applyFilters();
  }

  loadServiceItems(subcategoryId: string): void {
    this.serviceHierarchyService
      .getServiceItemsBySubcategory(+subcategoryId)
      .subscribe({
        next: (items) => {
          this.serviceItems = items.filter((item) => item.active);
        },
        error: (error) => {
          console.error('Error loading service items:', error);
        },
      });
  }

  onServiceItemChange(): void {
    this.applyFilters();
  }

  onPageChange(page: number): void {
    this.currentPage = page - 1;
    if (this.searchTerm.trim()) {
      this.searchTasks();
    } else {
      if (this.isStaff) {
        this.loadStaffTasks();
      } else if (this.isClient) {
        this.loadClientTasks();
      } else {
        this.loadTasks();
      }
    }
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    if (this.searchTerm.trim()) {
      this.searchTasks();
    } else {
      if (this.isStaff) {
        this.loadStaffTasks();
      } else if (this.isClient) {
        this.loadClientTasks();
      } else {
        this.loadTasks();
      }
    }
  }

  applyFilters(): void {
    this.currentPage = 0;
    if (this.isStaff) {
      this.loadStaffTasks();
    } else if (this.isClient) {
      this.loadClientTasks();
    } else {
      this.loadTasks();
    }
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.selectedPriority = '';
    this.selectedServiceCategory = '';
    this.selectedServiceSubcategory = '';
    this.selectedServiceItem = '';
    this.currentPage = 0;
    if (this.isStaff) {
      this.loadStaffTasks();
    } else if (this.isClient) {
      this.loadClientTasks();
    } else {
      this.loadTasks();
    }
  }

  // Modal methods
  openAddTaskModal(): void {
    this.selectedTask = null;
    this.showTaskModal = true;
  }

  openEditTaskModal(task: Task): void {
    // Fetch detailed task information including service data
    this.taskService.getTaskDetailById(task.id).subscribe({
      next: (detail) => {
        this.selectedTask = detail;
        this.showTaskModal = true;
      },
      error: (error) => {
        console.error('Error loading task details:', error);
        // Fallback to basic task data if detail fetch fails
        this.selectedTask = task;
        this.showTaskModal = true;
      },
    });
  }

  onTaskSaved(task: Task): void {
    this.showTaskModal = false;
    this.selectedTask = null;

    if (this.isStaff) {
      this.loadStaffTasks();
      this.loadStaffStatistics();
    } else if (this.isClient) {
      this.loadClientTasks();
      this.loadClientStatistics();
    } else {
      this.loadTasks();
      this.loadStatistics();
    }

    // Show success toast
    if (this.selectedTask) {
      this.toastService.showSuccess('Task updated successfully!');
    } else {
      this.toastService.showSuccess('Task created successfully!');
    }
  }

  onTaskModalCancelled(): void {
    this.showTaskModal = false;
    this.selectedTask = null;
  }

  openTaskDetailModal(task: Task): void {
    this.taskService.getTaskDetailById(task.id).subscribe({
      next: (detail) => {
        this.taskDetail = detail;
        this.showTaskDetailModal = true;
      },
      error: (error) => {
        this.error = 'Failed to load task details';
        console.error('Error loading task details:', error);
      },
    });
  }

  viewTaskDetails(task: Task): void {
    // Fetch detailed task information including service data
    this.taskService.getTaskDetailById(task.id).subscribe({
      next: (detail) => {
        this.selectedTaskForView = detail;
        this.showViewTaskModal = true;
      },
      error: (error) => {
        console.error('Error loading task details:', error);
        // Fallback to basic task data if detail fetch fails
        this.selectedTaskForView = task;
        this.showViewTaskModal = true;
      },
    });
  }

  closeViewTaskModal(): void {
    this.showViewTaskModal = false;
    this.selectedTaskForView = null;
  }

  openReassignModal(task: Task): void {
    this.selectedTask = task;
    this.newStaffId = null;
    this.showReassignModal = true;
  }

  reassignTask(): void {
    if (this.selectedTask && this.newStaffId) {
      this.taskService
        .reassignTask(this.selectedTask.id, this.newStaffId)
        .subscribe({
          next: () => {
            this.showReassignModal = false;
            this.selectedTask = null;
            this.newStaffId = null;
            this.loadTasks();
            this.toastService.showSuccess('Task reassigned successfully!');
          },
          error: (error) => {
            this.error = 'Failed to reassign task';
            console.error('Error reassigning task:', error);
            this.toastService.showError(
              'Failed to reassign task. Please try again.'
            );
          },
        });
    }
  }

  updateTaskStatus(task: Task, newStatus: Task['status']): void {
    this.taskService.updateTaskStatus(task.id, newStatus).subscribe({
      next: () => {
        this.loadTasks();
        this.toastService.showSuccess('Task status updated successfully!');
      },
      error: (error) => {
        this.error = 'Failed to update task status';
        console.error('Error updating task status:', error);
        this.toastService.showError(
          'Failed to update task status. Please try again.'
        );
      },
    });
  }

  openDeleteDialog(task: Task): void {
    this.taskToDelete = task;
    this.showDeleteDialog = true;
  }

  deleteTask(): void {
    if (this.taskToDelete) {
      this.taskService.deleteTask(this.taskToDelete.id).subscribe({
        next: () => {
          this.showDeleteDialog = false;
          this.taskToDelete = null;
          this.loadTasks(); // Refresh the list
          this.toastService.showSuccess('Task deleted successfully!');
        },
        error: (error: any) => {
          console.error('Error deleting task:', error);
          this.showDeleteDialog = false;
          this.taskToDelete = null;
          this.toastService.showError(
            'Failed to delete task. Please try again.'
          );
        },
      });
    }
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
    this.taskToDelete = null;
  }

  openStatisticsModal(): void {
    this.showStatisticsModal = true;
  }

  openWorkloadModal(): void {
    this.showWorkloadModal = true;
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'text-yellow-600 bg-yellow-100';
      case 'IN_PROGRESS':
        return 'text-blue-600 bg-blue-100';
      case 'COMPLETED':
        return 'text-green-600 bg-green-100';
      case 'ON_HOLD':
        return 'text-orange-600 bg-orange-100';
      case 'CANCELLED':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'LOW':
        return 'text-green-600 bg-green-100';
      case 'MEDIUM':
        return 'text-yellow-600 bg-yellow-100';
      case 'HIGH':
        return 'text-orange-600 bg-orange-100';
      case 'URGENT':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }

  getDeadlineStatusColor(status: string): string {
    switch (status) {
      case 'Overdue':
        return 'text-red-600 bg-red-100';
      case 'Due Soon':
        return 'text-orange-600 bg-orange-100';
      case 'Safe':
        return 'text-green-600 bg-green-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }

  clearError(): void {
    this.error = '';
  }

  // Email functionality
  openSendEmailModal(task: Task): void {
    this.selectedTaskForEmail = task;
    this.showSendEmailModal = true;
  }

  closeSendEmailModal(): void {
    this.showSendEmailModal = false;
    this.selectedTaskForEmail = null;
  }

  sendEmailToClient(): void {
    if (this.selectedTaskForEmail) {
      // Get client email from the task detail
      this.taskService
        .getTaskDetailById(this.selectedTaskForEmail.id)
        .subscribe({
          next: (taskDetail) => {
            if (taskDetail.clientEmail) {
              this.openGmailWithRecipient(taskDetail.clientEmail, 'client');
            } else {
              this.toastService.showError('Client email not available');
            }
          },
          error: (error) => {
            console.error('Error fetching task detail:', error);
            this.toastService.showError('Error fetching task details');
          },
        });
    }
  }

  sendEmailToStaff(): void {
    if (this.selectedTaskForEmail) {
      // Get staff email from the task detail
      this.taskService
        .getTaskDetailById(this.selectedTaskForEmail.id)
        .subscribe({
          next: (taskDetail) => {
            if (taskDetail.assignedStaffEmail) {
              // Check if current user is trying to email themselves
              if (
                this.isStaff &&
                taskDetail.assignedStaffEmail === this.currentUser?.email
              ) {
                this.toastService.showError(
                  'You cannot send an email to yourself'
                );
                return;
              }
              this.openGmailWithRecipient(
                taskDetail.assignedStaffEmail,
                'staff'
              );
            } else {
              this.toastService.showError('Staff email not available');
            }
          },
          error: (error) => {
            console.error('Error fetching task detail:', error);
            this.toastService.showError('Error fetching task details');
          },
        });
    }
  }

  sendEmailToAdmin(): void {
    if (this.selectedTaskForEmail) {
      // Get admin email from the task detail
      this.taskService
        .getTaskDetailById(this.selectedTaskForEmail.id)
        .subscribe({
          next: (taskDetail) => {
            if (taskDetail.createdByEmail) {
              this.openGmailWithRecipient(taskDetail.createdByEmail, 'admin');
            } else {
              this.toastService.showError('Admin email not available');
            }
          },
          error: (error) => {
            console.error('Error fetching task detail:', error);
            this.toastService.showError('Error fetching task details');
          },
        });
    }
  }

  private openGmailWithRecipient(
    recipientEmail: string,
    recipientType: 'client' | 'staff' | 'admin'
  ): void {
    const task = this.selectedTaskForEmail;
    if (!task) return;

    // Create email subject and body
    const subject = `Task Update: ${task.title}`;
    const body = `Dear ${
      recipientType === 'client'
        ? task.clientName
        : recipientType === 'admin'
        ? task.createdByName
        : task.assignedStaffName
    },

I hope this email finds you well. I am writing regarding the task "${
      task.title
    }" (${task.taskType}).

Task Details:
- Title: ${task.title}
- Status: ${task.status}
- Priority: ${task.priority}
- Due Date: ${task.dueDate}

Please let me know if you need any clarification or have questions.

Best regards,
${this.currentUser?.firstName} ${this.currentUser?.lastName}`;

    // Open Gmail with pre-filled content
    const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${recipientEmail}&su=${encodeURIComponent(
      subject
    )}&body=${encodeURIComponent(body)}`;
    window.open(gmailUrl, '_blank');
  }

  // Client-specific methods
  openAcknowledgeModal(task: any): void {
    this.selectedTaskForAcknowledge = task;
    this.showAcknowledgeModal = true;
  }

  closeAcknowledgeModal(): void {
    this.showAcknowledgeModal = false;
    this.selectedTaskForAcknowledge = null;
    this.acknowledgeMessage = '';
  }

  acknowledgeTask(): void {
    if (this.selectedTaskForAcknowledge && this.acknowledgeMessage.trim()) {
      // Here you would typically send the acknowledgment to the backend
      // For now, we'll just show a success message
      this.toastService.showSuccess('Acknowledgment sent successfully!');
      this.closeAcknowledgeModal();
    } else {
      this.toastService.showError('Please enter a message');
    }
  }

  sendMessageToStaff(task: any): void {
    if (task.assignedStaffEmail) {
      const subject = `Message regarding task: ${task.title}`;
      const body = `Dear ${task.assignedStaffName || 'Staff Member'},

I hope this email finds you well. I am writing regarding the task "${
        task.title
      }".

Task Details:
- Title: ${task.title}
- Status: ${task.status}
- Priority: ${task.priority}
- Due Date: ${task.dueDate}

Please let me know if you need any additional information or have any questions.

Best regards,
${this.currentUser?.firstName} ${this.currentUser?.lastName}`;

      const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${
        task.assignedStaffEmail
      }&su=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
      window.open(gmailUrl, '_blank');
    } else {
      this.toastService.showError('Staff email not available');
    }
  }

  viewTaskTimeline(task: any): void {
    // This would open a modal showing task timeline/history
    // For now, we'll just show task details
    this.selectedTaskForView = task;
    this.showViewTaskModal = true;
  }
}
