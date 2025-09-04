import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { API_CONFIG } from '../api.config';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import { ServiceHierarchyService } from '../services/service-hierarchy.service';
import { ConfirmDialogComponent } from '../components/confirm-dialog.component';
import { ToastNotificationComponent } from '../components/toast-notification.component';
import { LeadFormModalComponent } from './lead-form-modal.component';
import { LeadDetailModalComponent } from './lead-detail-modal.component';
import { StaffAssignmentModalComponent } from './staff-assignment-modal.component';
import { PaginationComponent } from '../pagination/pagination.component';

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

interface LeadResponse {
  content: Lead[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Component({
  selector: 'app-lead-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ConfirmDialogComponent,
    ToastNotificationComponent,
    LeadFormModalComponent,
    LeadDetailModalComponent,
    StaffAssignmentModalComponent,
    PaginationComponent,
  ],
  templateUrl: './lead-management.component.html',
  styleUrls: ['./lead-management.component.css'],
})
export class LeadManagementComponent implements OnInit {
  leads: Lead[] = [];
  loading = false;
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [10, 25, 50];

  // Make Math available in template
  Math = Math;

  // Search and filter
  searchTerm = '';
  statusFilter = '';
  priorityFilter = '';

  // Service hierarchy filters
  selectedServiceCategory = '';
  selectedServiceSubcategory = '';
  selectedServiceItem = '';

  // Service hierarchy data for filters
  serviceCategories: any[] = [];
  serviceSubcategories: any[] = [];
  serviceItems: any[] = [];

  error = '';

  // Modal states
  showDeleteDialog = false;
  leadToDelete: Lead | null = null;
  deleteDialogConfig = {
    title: 'Delete Lead',
    message:
      'Are you sure you want to delete this lead? This action cannot be undone.',
    confirmLabel: 'Delete',
    cancelLabel: 'Cancel',
  };

  // Modal states for lead management
  showLeadModal = false;
  showLeadDetailModal = false;
  showStaffAssignmentModal = false;
  selectedLead: Lead | null = null;
  isEditMode = false;

  // Status options
  statusOptions = [
    { value: '', label: 'All Status' },
    { value: 'NEW', label: 'New' },
    { value: 'CONTACTED', label: 'Contacted' },
    { value: 'IN_DISCUSSION', label: 'In Discussion' },
    { value: 'PROPOSAL_SENT', label: 'Proposal Sent' },
    { value: 'CONVERTED', label: 'Converted' },
    { value: 'LOST', label: 'Lost' },
  ];

  // Priority options
  priorityOptions = [
    { value: '', label: 'All Priorities' },
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' },
  ];

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private toastService: ToastService,
    private serviceHierarchyService: ServiceHierarchyService
  ) {}

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get isStaff(): boolean {
    return this.authService.isStaff();
  }

  currentStaffId: number | null = null;

  ngOnInit(): void {
    // Load service hierarchy data for filters
    this.loadServiceCategories();

    if (this.isStaff) {
      this.authService.getCurrentStaffId().subscribe({
        next: (staffId) => {
          this.currentStaffId = staffId;
          this.loadLeads();
        },
        error: (error) => {
          console.error('Error getting staff ID:', error);
          this.currentStaffId = null;
          this.loadLeads();
        },
      });
    } else {
      this.loadLeads();
    }
  }

  loadLeads(): void {
    this.loading = true;
    this.error = '';

    let params = new HttpParams()
      .set('page', this.currentPage.toString())
      .set('size', this.pageSize.toString());

    // For staff users, only show their assigned leads
    let url = `${API_CONFIG.baseUrl}/api/leads/filter`;
    if (this.isStaff && this.currentStaffId) {
      url = `${API_CONFIG.baseUrl}/api/leads/assigned/${this.currentStaffId}`;
    } else if (this.isStaff && !this.currentStaffId) {
      // If staff ID is not available, show empty list
      this.leads = [];
      this.totalElements = 0;
      this.totalPages = 0;
      this.loading = false;
      return;
    }

    // Add filters as query parameters for both staff and admin endpoints
    if (this.searchTerm.trim()) {
      params = params.set('search', this.searchTerm.trim());
    }
    if (this.statusFilter) {
      params = params.set('status', this.statusFilter);
    }
    if (this.priorityFilter) {
      params = params.set('priority', this.priorityFilter);
    }

    // Add service hierarchy filters
    if (this.selectedServiceCategory) {
      params = params.set('serviceCategoryId', this.selectedServiceCategory);
    }
    if (this.selectedServiceSubcategory) {
      params = params.set(
        'serviceSubcategoryId',
        this.selectedServiceSubcategory
      );
    }
    if (this.selectedServiceItem) {
      params = params.set('serviceItemId', this.selectedServiceItem);
    }

    this.http.get<LeadResponse>(url, { params }).subscribe({
      next: (response) => {
        this.leads = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading leads:', error);
        this.error = 'Error loading leads. Please try again.';
        this.loading = false;
      },
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadLeads();
  }

  onStatusFilterChange(): void {
    this.currentPage = 0;
    this.loadLeads();
  }

  onPriorityFilterChange(): void {
    this.currentPage = 0;
    this.loadLeads();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = '';
    this.priorityFilter = '';
    this.selectedServiceCategory = '';
    this.selectedServiceSubcategory = '';
    this.selectedServiceItem = '';
    this.serviceSubcategories = [];
    this.serviceItems = [];
    this.currentPage = 0;
    this.loadLeads();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadLeads();
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadLeads();
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const startPage = Math.max(0, this.currentPage - 2);
    const endPage = Math.min(this.totalPages - 1, this.currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }

  getStatusBadgeClass(status: string): string {
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

  getPriorityBadgeClass(priority: string): string {
    switch (priority) {
      case 'LOW':
        return 'bg-gray-100 text-gray-800';
      case 'MEDIUM':
        return 'bg-blue-100 text-blue-800';
      case 'HIGH':
        return 'bg-orange-100 text-orange-800';
      case 'URGENT':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString();
  }

  formatDateTime(dateString: string): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  }

  // Lead management methods
  viewLeadDetails(lead: Lead): void {
    // Get detailed lead information and show detail modal
    this.http
      .get<Lead>(`${API_CONFIG.baseUrl}/api/leads/${lead.id}`)
      .subscribe({
        next: (detailedLead) => {
          this.selectedLead = detailedLead;
          this.showLeadDetailModal = true;
        },
        error: (error) => {
          console.error('Error fetching lead details:', error);
          this.toastService.showError(
            'Failed to load lead details. Please try again.'
          );
        },
      });
  }

  openEditLeadModal(lead: Lead): void {
    // Allow both admin and staff to edit leads
    if (!this.isAdmin && !this.isStaff) {
      this.toastService.showError(
        'You do not have permission to edit lead information.'
      );
      return;
    }

    // Get detailed lead information and show edit modal
    this.http
      .get<Lead>(`${API_CONFIG.baseUrl}/api/leads/${lead.id}`)
      .subscribe({
        next: (detailedLead) => {
          this.selectedLead = detailedLead;
          this.isEditMode = true;
          this.showLeadModal = true;
        },
        error: (error) => {
          console.error('Error fetching lead for editing:', error);
          this.toastService.showError(
            'Failed to load lead data for editing. Please try again.'
          );
        },
      });
  }

  openAssignStaffModal(lead: Lead): void {
    if (!this.isAdmin) {
      this.toastService.showError('Only administrators can assign staff.');
      return;
    }

    this.selectedLead = lead;
    this.showStaffAssignmentModal = true;
  }

  // Modal event handlers
  onLeadSaved(updatedLead: Lead): void {
    this.showLeadModal = false;
    this.selectedLead = null;
    this.loadLeads(); // Refresh the list
  }

  onLeadModalCancelled(): void {
    this.showLeadModal = false;
    this.selectedLead = null;
  }

  onLeadDetailClosed(): void {
    this.showLeadDetailModal = false;
    this.selectedLead = null;
  }

  onStaffAssigned(updatedLead: Lead): void {
    this.showStaffAssignmentModal = false;
    this.selectedLead = null;
    this.loadLeads(); // Refresh the list
  }

  onStaffAssignmentCancelled(): void {
    this.showStaffAssignmentModal = false;
    this.selectedLead = null;
  }

  deleteLead(lead: Lead): void {
    if (!this.isAdmin) {
      this.toastService.showError('Only administrators can delete leads.');
      return;
    }

    this.leadToDelete = lead;
    this.selectedLead = null; // Clear any existing convert target
    this.deleteDialogConfig = {
      title: 'Delete Lead',
      message: `Are you sure you want to delete the lead for ${lead.firstName} ${lead.lastName}? This action cannot be undone.`,
      confirmLabel: 'Delete',
      cancelLabel: 'Cancel',
    };
    this.showDeleteDialog = true;
  }

  convertToClient(lead: Lead): void {
    // Show confirmation dialog
    this.deleteDialogConfig = {
      title: 'Convert Lead to Client',
      message: `Are you sure you want to convert "${lead.firstName} ${lead.lastName}" to a client? This will create a new client account and update the lead status to CONVERTED.`,
      confirmLabel: 'Convert',
      cancelLabel: 'Cancel',
    };
    this.selectedLead = lead;
    this.leadToDelete = null; // Clear any existing delete target
    this.showDeleteDialog = true;
  }

  confirmDelete(): void {
    // Check if this is a convert operation or delete operation
    if (this.deleteDialogConfig.title === 'Convert Lead to Client') {
      if (!this.selectedLead) return;

      const lead = this.selectedLead;
      this.loading = true;
      this.http
        .post(`${API_CONFIG.baseUrl}/api/leads/${lead.id}/convert`, {})
        .subscribe({
          next: (response: any) => {
            this.toastService.showSuccess(
              `Lead "${lead.firstName} ${lead.lastName}" successfully converted to client!`
            );
            this.loadLeads(); // Refresh the list
            this.loading = false;
            this.showDeleteDialog = false;
            this.selectedLead = null;
          },
          error: (error) => {
            console.error('Error converting lead to client:', error);
            this.toastService.showError(
              error.error || 'Failed to convert lead to client'
            );
            this.loading = false;
            this.showDeleteDialog = false;
            this.selectedLead = null;
          },
        });
    } else if (this.leadToDelete) {
      this.http
        .delete(`${API_CONFIG.baseUrl}/api/leads/${this.leadToDelete.id}`)
        .subscribe({
          next: () => {
            this.showDeleteDialog = false;
            this.leadToDelete = null;
            this.toastService.showSuccess('Lead deleted successfully!');
            this.loadLeads(); // Refresh the list
          },
          error: (error) => {
            console.error('Error deleting lead:', error);
            this.showDeleteDialog = false;
            this.leadToDelete = null;
            this.toastService.showError(
              'Failed to delete lead. Please try again.'
            );
          },
        });
    }
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
    this.leadToDelete = null;
    this.selectedLead = null;
  }

  // Additional utility method for updating lead status
  updateLeadStatus(leadId: number, status: string): void {
    const params = new HttpParams().set('status', status);

    this.http
      .put<Lead>(
        `${API_CONFIG.baseUrl}/api/leads/${leadId}/status`,
        {},
        { params }
      )
      .subscribe({
        next: (updatedLead) => {
          this.toastService.showSuccess(`Lead status updated to ${status}`);
          this.loadLeads(); // Refresh the list
        },
        error: (error) => {
          console.error('Error updating lead status:', error);
          this.toastService.showError(
            'Failed to update lead status. Please try again.'
          );
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

  applyFilters(): void {
    this.currentPage = 0;
    this.loadLeads();
  }
}
