import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  DeadlineService,
  Deadline,
  DeadlineFilters,
  DeadlineStatistics,
} from '../services/deadline.service';
import { PaginationComponent } from '../pagination/pagination.component';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-deadline-management',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationComponent],
  templateUrl: './deadline-management.component.html',
  styleUrls: ['./deadline-management.component.scss'],
})
export class DeadlineManagementComponent implements OnInit {
  deadlines: Deadline[] = [];
  statistics: DeadlineStatistics | null = null;
  loading = false;
  error: string | null = null;

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;

  // Filters
  filters: DeadlineFilters = {
    status: '',
    priority: '',
    taskType: '',
    isOverdue: undefined,
    searchTerm: '',
    page: 0,
    size: 10,
  };

  // Filter options
  statusOptions = [
    { value: '', label: 'All Status' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'OVERDUE', label: 'Overdue' },
  ];

  priorityOptions = [
    { value: '', label: 'All Priorities' },
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' },
  ];

  taskTypeOptions = [
    { value: '', label: 'All Types' },
    { value: 'ITR_FILING', label: 'ITR Filing' },
    { value: 'GST_RETURN', label: 'GST Return' },
    { value: 'COMPANY_REGISTRATION', label: 'Company Registration' },
    { value: 'TAX_AUDIT', label: 'Tax Audit' },
    { value: 'OTHER', label: 'Other' },
  ];

  overdueFilterOptions = [
    { value: undefined, label: 'All Deadlines' },
    { value: true, label: 'Overdue Only' },
    { value: false, label: 'Not Overdue' },
  ];

  constructor(
    private deadlineService: DeadlineService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadDeadlines();
    this.loadStatistics();
  }

  loadDeadlines(): void {
    this.loading = true;
    this.error = null;

    this.deadlineService.getDeadlines(this.filters).subscribe({
      next: (response) => {
        this.deadlines = response.content;
        this.totalItems = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.pageNumber;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load deadlines';
        this.loading = false;
        console.error('Error loading deadlines:', error);
        this.toastService.showError(
          'Failed to load deadlines. Please try again.'
        );
      },
    });
  }

  loadStatistics(): void {
    this.deadlineService.getDeadlineStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (error) => {
        console.error('Error loading deadline statistics:', error);
      },
    });
  }

  applyFilters(): void {
    this.filters.page = 0;
    this.currentPage = 0;
    this.loadDeadlines();
  }

  clearFilters(): void {
    this.filters = {
      status: '',
      priority: '',
      taskType: '',
      isOverdue: undefined,
      searchTerm: '',
      page: 0,
      size: this.pageSize,
    };
    this.loadDeadlines();
  }

  searchDeadlines(): void {
    this.filters.page = 0;
    this.currentPage = 0;
    this.loadDeadlines();
  }

  onPageChange(page: number): void {
    this.currentPage = page - 1;
    this.filters.page = this.currentPage;
    this.loadDeadlines();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.filters.size = size;
    this.filters.page = 0;
    this.currentPage = 0;
    this.loadDeadlines();
  }

  updateDeadlineStatus(deadline: Deadline, newStatus: string): void {
    this.deadlineService
      .updateDeadlineStatus(deadline.id, newStatus)
      .subscribe({
        next: (updatedDeadline) => {
          const index = this.deadlines.findIndex((d) => d.id === deadline.id);
          if (index !== -1) {
            this.deadlines[index] = updatedDeadline;
          }
          this.toastService.showSuccess(
            `Deadline status updated to ${newStatus}`
          );
          this.loadStatistics(); // Refresh statistics
        },
        error: (error) => {
          console.error('Error updating deadline status:', error);
          this.toastService.showError('Failed to update deadline status');
        },
      });
  }

  extendDeadline(deadline: Deadline): void {
    // This would typically open a modal to select a new date
    const newDueDate = prompt('Enter new due date (YYYY-MM-DD):');
    if (newDueDate) {
      this.deadlineService.extendDeadline(deadline.id, newDueDate).subscribe({
        next: (updatedDeadline) => {
          const index = this.deadlines.findIndex((d) => d.id === deadline.id);
          if (index !== -1) {
            this.deadlines[index] = updatedDeadline;
          }
          this.toastService.showSuccess('Deadline extended successfully');
          this.loadStatistics();
        },
        error: (error) => {
          console.error('Error extending deadline:', error);
          this.toastService.showError('Failed to extend deadline');
        },
      });
    }
  }

  sendReminder(deadline: Deadline): void {
    this.deadlineService.sendDeadlineReminder(deadline.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Reminder sent successfully');
      },
      error: (error) => {
        console.error('Error sending reminder:', error);
        this.toastService.showError('Failed to send reminder');
      },
    });
  }

  getTaskStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'IN_PROGRESS':
        return 'bg-blue-100 text-blue-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'OVERDUE':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'URGENT':
        return 'bg-red-100 text-red-800';
      case 'HIGH':
        return 'bg-orange-100 text-orange-800';
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-800';
      case 'LOW':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getDeadlineStatusColor(deadlineStatus: string): string {
    switch (deadlineStatus) {
      case 'OVERDUE':
        return 'bg-red-100 text-red-800';
      case 'DUE_SOON':
        return 'bg-orange-100 text-orange-800';
      case 'SAFE':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getDaysRemainingText(daysRemaining: number): string {
    if (daysRemaining < 0) {
      return `${Math.abs(daysRemaining)} days overdue`;
    } else if (daysRemaining === 0) {
      return 'Due today';
    } else if (daysRemaining === 1) {
      return 'Due tomorrow';
    } else {
      return `${daysRemaining} days remaining`;
    }
  }

  clearError(): void {
    this.error = null;
  }
}
