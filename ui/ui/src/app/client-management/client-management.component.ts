import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import {
  ClientService,
  Client,
  ClientDetail,
} from '../services/client.service';
import { PaginationComponent } from '../pagination/pagination.component';
import { ClientFormModalComponent } from './client-form-modal.component';
import { ConfirmDialogComponent } from '../components/confirm-dialog.component';
import { ToastService } from '../services/toast.service';
import { AuthService, StoredUser } from '../services/auth.service';
import { API_CONFIG } from '../api.config';
import { DateFormatPipe } from '../shared/pipes/date-format.pipe';

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
  selector: 'app-client-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PaginationComponent,
    ConfirmDialogComponent,
    ClientFormModalComponent,
    DateFormatPipe,
  ],
  templateUrl: './client-management.component.html',
  styleUrls: ['./client-management.component.scss'],
})
export class ClientManagementComponent implements OnInit {
  clients: Client[] = [];
  loading = false;
  error: string | null = null;

  // Role-based properties
  currentUser: StoredUser | null = null;
  isStaff = false;
  isAdmin = false;

  // Pagination
  currentPage = 0;
  pageSize = 5;
  totalElements = 0;

  // Search and filters
  searchTerm = '';
  statusFilter = '';

  // Modal state
  showClientModal = false;
  selectedClient: Client | null = null;
  showViewModal = false;
  showAssignStaffModal = false;
  selectedClientForView: ClientDetail | null = null;
  selectedClientForAssign: Client | null = null;
  selectedStaffId: number | null = null;
  availableStaff: Staff[] = [];

  // Delete confirmation dialog
  showDeleteDialog = false;
  clientToDelete: Client | null = null;

  // Client task and document data (for staff view)
  clientTasks: any[] = [];
  clientDocuments: any[] = [];

  constructor(
    private clientService: ClientService,
    private http: HttpClient,
    private toastService: ToastService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.isStaff = this.authService.isStaff();
    this.isAdmin = this.authService.isAdmin();

    this.loadClients();
    if (this.isAdmin) {
      this.loadStaff();
    }
  }

  loadStaff(): void {
    this.http
      .get<Staff[]>(`${API_CONFIG.baseUrl}${API_CONFIG.staff}`)
      .subscribe({
        next: (staff) => {
          this.availableStaff = staff;
        },
        error: (error) => {
          console.error('Error loading staff:', error);
          this.availableStaff = [];
        },
      });
  }

  loadClients(): void {
    this.loading = true;
    this.error = null;

    this.clientService
      .getClients(
        this.currentPage,
        this.pageSize,
        this.searchTerm,
        this.statusFilter
      )
      .subscribe({
        next: (response) => {
          this.clients = response.content;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Failed to load clients';
          this.loading = false;
          console.error('Error loading clients:', error);
        },
      });
  }

  onPageChange(page: number): void {
    this.currentPage = page - 1;
    this.loadClients();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadClients();
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadClients();
  }

  onStatusFilterChange(): void {
    this.currentPage = 0;
    this.loadClients();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = '';
    this.currentPage = 0;
    this.loadClients();
  }

  // Modal methods
  openAddClientModal(): void {
    if (!this.isAdmin) {
      this.toastService.showError('Only administrators can add new clients.');
      return;
    }
    this.selectedClient = null;
    this.showClientModal = true;
  }

  openEditClientModal(client: Client | ClientDetail): void {
    if (!this.isAdmin) {
      this.toastService.showError(
        'Only administrators can edit client information.'
      );
      return;
    }

    // Convert ClientDetail to Client if needed
    const clientForEdit: Client = {
      id: client.id,
      userId: client.userId,
      email: client.email,
      firstName: client.firstName,
      lastName: client.lastName,
      phone: client.phone,
      isActive: client.isActive,
      emailVerified: 'emailVerified' in client ? client.emailVerified : false,
      lastLogin: 'lastLogin' in client ? client.lastLogin : '',
      companyName: client.companyName,
      companyType: client.companyType,
      gstNumber: client.gstNumber,
      panNumber: client.panNumber,
      address: client.address,
      city: client.city,
      state: client.state,
      pincode: client.pincode,
      country: client.country,
      businessType: client.businessType,
      industry: client.industry,
      website: client.website,
      contactPerson: client.contactPerson,
      contactPhone: client.contactPhone,
      contactEmail: client.contactEmail,
      emergencyContact: client.emergencyContact,
      clientType: client.clientType,
      registrationDate: client.registrationDate,
      createdAt: client.createdAt,
      updatedAt: client.updatedAt,
      totalTasks: 'totalTasks' in client ? client.totalTasks : 0,
      completedTasks: 'completedTasks' in client ? client.completedTasks : 0,
      pendingTasks: 'pendingTasks' in client ? client.pendingTasks : 0,
      assignedStaffName: client.assignedStaffName,
      assignedStaffId: client.assignedStaffId,
    };

    this.selectedClient = clientForEdit;
    this.showClientModal = true;
  }

  onClientSaved(client: Client): void {
    this.showClientModal = false;
    this.selectedClient = null;
    this.loadClients(); // Refresh the list

    // Show success toast
    if (this.selectedClient) {
      this.toastService.showSuccess('Client updated successfully!');
    } else {
      this.toastService.showSuccess('Client created successfully!');
    }
  }

  onClientModalCancelled(): void {
    this.showClientModal = false;
    this.selectedClient = null;
  }

  viewClientDetails(client: Client): void {
    this.clientService.getClientById(client.id).subscribe({
      next: (clientDetail) => {
        this.selectedClientForView = clientDetail;
        this.showViewModal = true;

        // Load additional data for staff view
        if (this.isStaff) {
          this.loadClientTasks(client.id);
          this.loadClientDocuments(client.id);
        }
      },
      error: (error) => {
        console.error('Error fetching client details:', error);
        // You might want to show an error message to the user
      },
    });
  }

  loadClientTasks(clientId: number): void {
    this.clientService.getClientTasks(clientId, 0, 10).subscribe({
      next: (response) => {
        this.clientTasks = response.content || [];
      },
      error: (error) => {
        console.error('Error loading client tasks:', error);
        this.clientTasks = [];
      },
    });
  }

  loadClientDocuments(clientId: number): void {
    this.clientService.getClientDocuments(clientId, 0, 10).subscribe({
      next: (response) => {
        this.clientDocuments = response.content || [];
      },
      error: (error) => {
        console.error('Error loading client documents:', error);
        this.clientDocuments = [];
      },
    });
  }

  openAssignStaffModal(client: Client): void {
    if (!this.isAdmin) {
      this.toastService.showError(
        'Only administrators can assign staff to clients.'
      );
      return;
    }

    this.selectedClientForAssign = client;
    this.selectedStaffId = client.assignedStaffId || null;
    this.showAssignStaffModal = true;
  }

  assignStaffToClient(): void {
    if (this.selectedClientForAssign && this.selectedStaffId) {
      this.clientService
        .assignStaffToClient(
          this.selectedClientForAssign.id,
          this.selectedStaffId
        )
        .subscribe({
          next: () => {
            this.showAssignStaffModal = false;
            this.selectedClientForAssign = null;
            this.selectedStaffId = null;
            this.loadClients(); // Refresh the list
            this.toastService.showSuccess(
              'Staff assigned to client successfully!'
            );
          },
          error: (error) => {
            console.error('Error assigning staff:', error);
            this.toastService.showError(
              'Failed to assign staff to client. Please try again.'
            );
          },
        });
    }
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedClientForView = null;
  }

  closeAssignStaffModal(): void {
    this.showAssignStaffModal = false;
    this.selectedClientForAssign = null;
    this.selectedStaffId = null;
  }

  toggleClientStatus(client: Client): void {
    if (!this.isAdmin) {
      this.toastService.showError(
        'Only administrators can change client status.'
      );
      return;
    }

    this.clientService.toggleClientStatus(client.id).subscribe({
      next: () => {
        this.loadClients(); // Refresh the list
        this.toastService.showSuccess('Client status updated successfully!');
      },
      error: (error) => {
        console.error('Error toggling client status:', error);
        this.toastService.showError(
          'Failed to update client status. Please try again.'
        );
      },
    });
  }

  deleteClient(client: Client): void {
    if (!this.isAdmin) {
      this.toastService.showError('Only administrators can delete clients.');
      return;
    }

    this.clientToDelete = client;
    this.showDeleteDialog = true;
  }

  confirmDelete(): void {
    if (this.clientToDelete) {
      this.clientService.deleteClient(this.clientToDelete.id).subscribe({
        next: () => {
          this.showDeleteDialog = false;
          this.clientToDelete = null;
          this.loadClients(); // Refresh the list
          this.toastService.showSuccess('Client deleted successfully!');
        },
        error: (error) => {
          console.error('Error deleting client:', error);
          this.showDeleteDialog = false;
          this.clientToDelete = null;
          this.toastService.showError(
            'Failed to delete client. Please try again.'
          );
        },
      });
    }
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
    this.clientToDelete = null;
  }
}
