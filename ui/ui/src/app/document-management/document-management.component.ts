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
  DocumentService,
  Document,
  DocumentType,
  DocumentStatistics,
  DocumentFilters,
} from '../services/document.service';
import { ClientService, Client } from '../services/client.service';
import { TaskService, Task } from '../services/task.service';
import { AuthService } from '../services/auth.service';
import { PaginationComponent } from '../pagination/pagination.component';
import { ConfirmDialogComponent } from '../components/confirm-dialog.component';
import { ToastService } from '../services/toast.service';
import { API_CONFIG } from '../api.config';
import { DateFormatPipe } from '../shared/pipes/date-format.pipe';

@Component({
  selector: 'app-document-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PaginationComponent,
    ConfirmDialogComponent,
    DateFormatPipe,
  ],
  templateUrl: './document-management.component.html',
  styleUrls: ['./document-management.component.scss'],
})
export class DocumentManagementComponent implements OnInit {
  documents: any[] = [];
  clients: Client[] = [];
  tasks: Task[] = [];
  statistics: DocumentStatistics | null = null;
  loading = false;
  error: string | null = null;

  // Role-based access
  isClient = false;
  currentUser: any = null;

  // Pagination
  currentPage = 0;
  pageSize = 5;
  totalItems = 0;

  // Search and filters
  searchTerm = '';
  selectedClientId: number | null = null;
  selectedTaskId: number | null = null;
  selectedDocumentType: string = '';
  selectedVerificationStatus: string = '';

  // Upload modal
  showUploadModal = false;
  uploadForm: FormGroup;
  selectedFile: File | null = null;
  uploadProgress = 0;
  isUploading = false;

  // Client-specific task filtering
  filteredTasks: Task[] = [];
  selectedClientForTasks: number | null = null;

  // View modal
  showViewModal = false;
  selectedDocument: Document | null = null;

  // Delete confirmation
  showDeleteDialog = false;
  documentToDelete: Document | null = null;

  // Document types for dropdown
  documentTypes = [
    { value: DocumentType.PAN_CARD, label: 'PAN Card' },
    { value: DocumentType.AADHAR, label: 'Aadhaar Card' },
    { value: DocumentType.BANK_STATEMENT, label: 'Bank Statement' },
    { value: DocumentType.INVOICE, label: 'Invoice' },
    { value: DocumentType.FORM_16, label: 'Form 16' },
    { value: DocumentType.GST_RETURN, label: 'GST Return' },
    { value: DocumentType.COMPANY_DOCS, label: 'Company Documents' },
    { value: DocumentType.OTHER, label: 'Other' },
  ];

  // Verification status options
  verificationStatuses = [
    { value: '', label: 'All Status' },
    { value: 'VERIFIED', label: 'Verified' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'REJECTED', label: 'Rejected' },
  ];

  constructor(
    private documentService: DocumentService,
    private clientService: ClientService,
    private taskService: TaskService,
    public authService: AuthService,
    private fb: FormBuilder,
    private toastService: ToastService
  ) {
    this.uploadForm = this.fb.group({
      clientId: [null],
      taskId: [null, Validators.required],
      documentType: ['', Validators.required],
      file: [null, Validators.required],
    });
  }

  ngOnInit(): void {
    // Check user role and set up component accordingly
    this.currentUser = this.authService.getUserInfo();
    this.isClient = this.authService.isClient();

    // Initialize filtered tasks
    this.filteredTasks = [];

    if (this.isClient) {
      // Client view - load only client-specific data
      this.loadClientDocuments();
      this.loadClientStatistics();
      this.loadClientTasks(); // Load client's tasks for document upload
    } else if (this.authService.isStaff()) {
      // Staff view - load staff-specific data
      this.loadStaffDocuments();
      this.loadStaffStatistics();
      this.loadStaffTasks(); // Load tasks assigned to staff for document upload
      this.loadAssignedClients(); // Load clients assigned to staff for document upload
    } else {
      // Admin view - load all data
      this.loadDocuments();
      this.loadClients();
      this.loadTasks();
      this.loadStatistics();
    }
  }

  loadDocuments(): void {
    this.loading = true;
    this.error = null;

    const filters: DocumentFilters = {
      page: this.currentPage,
      size: this.pageSize,
    };

    // Include search term in filters if it exists
    if (this.searchTerm.trim()) {
      filters.searchTerm = this.searchTerm.trim();
    }

    // Include other filter parameters
    if (this.selectedClientId)
      filters.clientId = this.selectedClientId.toString();
    if (this.selectedTaskId) filters.taskId = this.selectedTaskId.toString();
    if (this.selectedDocumentType)
      filters.documentType = this.selectedDocumentType;
    if (this.selectedVerificationStatus)
      filters.isVerified = this.selectedVerificationStatus;

    this.documentService.getDocumentsWithFilters(filters).subscribe({
      next: (response) => {
        this.documents = response.content || response || [];
        this.totalItems =
          response.totalElements || response.length || this.documents.length;
        this.loading = false;
      },
      error: (error: any) => {
        this.error = 'Failed to load documents';
        this.loading = false;
        console.error('Error loading documents:', error);
      },
    });
  }

  loadClients(): void {
    this.clientService.getClients(0, 1000).subscribe({
      next: (response: any) => {
        this.clients = response.content || [];
      },
      error: (error: any) => {
        console.error('Error loading clients:', error);
      },
    });
  }

  loadAssignedClients(): void {
    // For staff users, this will automatically filter to only assigned clients
    this.clientService.getClients(0, 1000).subscribe({
      next: (response: any) => {
        this.clients = response.content || [];
      },
      error: (error: any) => {
        console.error('Error loading assigned clients:', error);
      },
    });
  }

  loadTasks(): void {
    this.taskService
      .getTasks({
        page: 0,
        size: 1000,
      })
      .subscribe({
        next: (response: any) => {
          this.tasks = response.content || [];
          this.filteredTasks = [...this.tasks]; // Initialize filtered tasks
        },
        error: (error: any) => {
          console.error('Error loading tasks:', error);
        },
      });
  }

  // Handle client selection change in upload modal
  onClientSelectionChange(): void {
    const selectedClientId = this.uploadForm.get('clientId')?.value;
    this.selectedClientForTasks = selectedClientId;

    if (selectedClientId) {
      if (this.authService.isStaff()) {
        // For staff users, get tasks assigned to them for the selected client
        this.authService.getCurrentStaffId().subscribe({
          next: (staffId) => {
            if (staffId) {
              this.taskService
                .getTasksByStaffAndClient(staffId, selectedClientId, 0, 1000)
                .subscribe({
                  next: (response: any) => {
                    this.filteredTasks = response.content || [];
                  },
                  error: (error: any) => {
                    console.error(
                      'Error loading staff tasks for client:',
                      error
                    );
                    this.filteredTasks = [];
                  },
                });
            } else {
              this.filteredTasks = [];
            }
          },
          error: (error: any) => {
            console.error('Error getting staff ID:', error);
            this.filteredTasks = [];
          },
        });
      } else {
        // For admin users, filter tasks for the selected client
        this.filteredTasks = this.tasks.filter(
          (task) => task.clientId === selectedClientId
        );
      }
    } else {
      // If no client selected, show all tasks
      this.filteredTasks = [...this.tasks];
    }

    // Reset task selection when client changes
    this.uploadForm.patchValue({ taskId: null });
  }

  // Handle client selection change in main filters
  onClientFilterChange(): void {
    this.selectedClientForTasks = this.selectedClientId;

    if (this.selectedClientId) {
      if (this.authService.isStaff()) {
        // For staff users, get tasks assigned to them for the selected client
        this.authService.getCurrentStaffId().subscribe({
          next: (staffId) => {
            if (staffId) {
              this.taskService
                .getTasksByStaffAndClient(
                  staffId,
                  this.selectedClientId!,
                  0,
                  1000
                )
                .subscribe({
                  next: (response: any) => {
                    this.filteredTasks = response.content || [];
                  },
                  error: (error: any) => {
                    console.error(
                      'Error loading staff tasks for client:',
                      error
                    );
                    this.filteredTasks = [];
                  },
                });
            } else {
              this.filteredTasks = [];
            }
          },
          error: (error: any) => {
            console.error('Error getting staff ID:', error);
            this.filteredTasks = [];
          },
        });
      } else {
        // For admin users, filter tasks for the selected client in main filters
        this.filteredTasks = this.tasks.filter(
          (task) => task.clientId === this.selectedClientId
        );
      }
    } else {
      // If no client selected, show all tasks
      this.filteredTasks = [...this.tasks];
    }

    // Reset task filter when client changes
    this.selectedTaskId = null;
  }

  loadStatistics(): void {
    this.documentService.getDocumentStatistics().subscribe({
      next: (statistics) => {
        this.statistics = statistics;
      },
      error: (error: any) => {
        console.error('Error loading statistics:', error);
      },
    });
  }

  searchDocuments(): void {
    this.currentPage = 0; // Reset to first page when searching
    this.loadDocuments(); // This will now include the search term
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadDocuments();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedClientId = null;
    this.selectedTaskId = null;
    this.selectedDocumentType = '';
    this.selectedVerificationStatus = '';
    this.currentPage = 0;
    this.loadDocuments();
  }

  openUploadModal(): void {
    this.showUploadModal = true;
    this.uploadForm.reset();
    this.selectedFile = null;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
    this.uploadForm.reset();
    this.selectedFile = null;
    this.isUploading = false;
    this.uploadProgress = 0;
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.uploadForm.patchValue({ file: file });
    }
  }

  uploadDocument(): void {
    if (this.isFormValid() && this.selectedFile) {
      this.isUploading = true;
      this.uploadProgress = 0;

      const formData = this.uploadForm.value;

      if (this.isClient) {
        // Client upload - use client-specific endpoint
        this.documentService
          .uploadMyDocument(
            this.selectedFile,
            formData.documentType,
            formData.taskId || undefined
          )
          .subscribe({
            next: (document) => {
              this.uploadProgress = 100;
              this.documents.unshift(document);
              this.closeUploadModal();
              this.loadClientStatistics();
              this.toastService.showSuccess('Document uploaded successfully!');
            },
            error: (error: any) => {
              this.error = 'Failed to upload document';
              this.isUploading = false;
              console.error('Error uploading document:', error);
              this.toastService.showError(
                'Failed to upload document. Please try again.'
              );
            },
          });
      } else if (this.authService.isStaff()) {
        // Staff upload - use staff-specific endpoint
        this.documentService
          .uploadStaffDocument(
            this.selectedFile,
            formData.clientId,
            formData.documentType,
            formData.taskId || undefined
          )
          .subscribe({
            next: (document) => {
              this.uploadProgress = 100;
              this.documents.unshift(document);
              this.closeUploadModal();
              this.loadStaffStatistics();
              this.toastService.showSuccess('Document uploaded successfully!');
            },
            error: (error: any) => {
              this.error = 'Failed to upload document';
              this.isUploading = false;
              console.error('Error uploading document:', error);
              this.toastService.showError(
                'Failed to upload document. Please try again.'
              );
            },
          });
      } else {
        // Admin/Staff upload - use regular endpoint
        this.documentService
          .uploadDocument(
            this.selectedFile,
            formData.clientId,
            formData.documentType,
            formData.taskId || undefined
          )
          .subscribe({
            next: (document) => {
              this.uploadProgress = 100;
              this.documents.unshift(document);
              this.closeUploadModal();
              this.loadStatistics();
              this.toastService.showSuccess('Document uploaded successfully!');
            },
            error: (error: any) => {
              this.error = 'Failed to upload document';
              this.isUploading = false;
              console.error('Error uploading document:', error);
              this.toastService.showError(
                'Failed to upload document. Please try again.'
              );
            },
          });
      }
    }
  }

  viewDocument(documentItem: Document): void {
    this.selectedDocument = documentItem;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedDocument = null;
  }

  downloadDocument(documentItem: Document): void {
    const downloadService = this.isClient
      ? this.documentService.downloadMyDocument(documentItem.id)
      : this.documentService.downloadDocument(documentItem.id);

    downloadService.subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = window.document.createElement('a');
        link.href = url;
        link.download = documentItem.originalFileName;
        link.click();
        window.URL.revokeObjectURL(url);
        this.toastService.showSuccess('Document downloaded successfully!');
      },
      error: (error: any) => {
        this.error = 'Failed to download document';
        console.error('Error downloading document:', error);
        this.toastService.showError(
          'Failed to download document. Please try again.'
        );
      },
    });
  }

  verifyDocument(documentItem: Document): void {
    this.documentService
      .updateDocumentVerification(documentItem.id, true)
      .subscribe({
        next: (updatedDocument) => {
          const index = this.documents.findIndex(
            (d) => d.id === documentItem.id
          );
          if (index !== -1) {
            this.documents[index] = updatedDocument;
          }
          this.loadStatistics();

          // Show appropriate message based on the action
          if (updatedDocument.status === 'VERIFIED') {
            this.toastService.showSuccess('Document verified successfully!');
          } else {
            this.toastService.showSuccess('Document unverified successfully!');
          }
        },
        error: (error: any) => {
          this.error = 'Failed to update document verification';
          console.error('Error updating document verification:', error);
          this.toastService.showError(
            'Failed to update document verification. Please try again.'
          );
        },
      });
  }

  // Rejection modal
  showRejectModal = false;
  documentToReject: Document | null = null;
  rejectionReason = '';

  rejectDocument(documentItem: Document): void {
    // If document is already rejected, unreject it directly without modal
    if (documentItem.status === 'REJECTED') {
      this.documentService.rejectDocument(documentItem.id, '').subscribe({
        next: (updatedDocument) => {
          const index = this.documents.findIndex(
            (d) => d.id === documentItem.id
          );
          if (index !== -1) {
            this.documents[index] = updatedDocument;
          }
          this.loadStatistics();
          this.toastService.showSuccess('Document unrejected successfully!');
        },
        error: (error: any) => {
          this.error = 'Failed to unreject document';
          console.error('Error unrejecting document:', error);
          this.toastService.showError(
            'Failed to unreject document. Please try again.'
          );
        },
      });
    } else {
      // Show modal for rejection reason
      this.documentToReject = documentItem;
      this.rejectionReason = '';
      this.showRejectModal = true;
    }
  }

  confirmReject(): void {
    if (this.documentToReject && this.rejectionReason.trim()) {
      this.documentService
        .rejectDocument(this.documentToReject.id, this.rejectionReason.trim())
        .subscribe({
          next: (updatedDocument) => {
            const index = this.documents.findIndex(
              (d) => d.id === this.documentToReject!.id
            );
            if (index !== -1) {
              this.documents[index] = updatedDocument;
            }
            this.loadStatistics();
            this.showRejectModal = false;
            this.documentToReject = null;
            this.rejectionReason = '';

            // Show appropriate message based on the action
            if (updatedDocument.status === 'REJECTED') {
              this.toastService.showSuccess('Document rejected successfully!');
            } else {
              this.toastService.showSuccess(
                'Document unrejected successfully!'
              );
            }
          },
          error: (error: any) => {
            this.error = 'Failed to update document rejection';
            console.error('Error updating document rejection:', error);
            this.toastService.showError(
              'Failed to update document rejection. Please try again.'
            );
          },
        });
    }
  }

  cancelReject(): void {
    this.showRejectModal = false;
    this.documentToReject = null;
    this.rejectionReason = '';
  }

  openDeleteDialog(documentItem: Document): void {
    this.documentToDelete = documentItem;
    this.showDeleteDialog = true;
  }

  deleteDocument(): void {
    if (this.documentToDelete) {
      this.documentService.deleteDocument(this.documentToDelete.id).subscribe({
        next: () => {
          this.documents = this.documents.filter(
            (d) => d.id !== this.documentToDelete!.id
          );
          this.showDeleteDialog = false;
          this.documentToDelete = null;
          this.loadStatistics();
          this.toastService.showSuccess('Document deleted successfully!');
        },
        error: (error: any) => {
          this.error = 'Failed to delete document';
          console.error('Error deleting document:', error);
          this.toastService.showError(
            'Failed to delete document. Please try again.'
          );
        },
      });
    }
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
    this.documentToDelete = null;
  }

  onPageChange(page: number): void {
    this.currentPage = page - 1;
    if (this.searchTerm.trim()) {
      this.searchDocuments();
    } else {
      this.loadDocuments();
    }
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    if (this.searchTerm.trim()) {
      this.searchDocuments();
    } else {
      this.loadDocuments();
    }
  }

  clearError(): void {
    this.error = null;
  }

  getDocumentTypeDisplayName(documentType: DocumentType): string {
    return this.documentService.getDocumentTypeDisplayName(documentType);
  }

  formatFileSize(bytes: number): string {
    return this.documentService.formatFileSize(bytes);
  }

  getFileIcon(fileType: string): string {
    return this.documentService.getFileIcon(fileType);
  }

  getVerificationStatusColor(status: string): string {
    switch (status) {
      case 'VERIFIED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'PENDING':
      default:
        return 'bg-yellow-100 text-yellow-800';
    }
  }

  getVerificationStatusText(status: string): string {
    switch (status) {
      case 'VERIFIED':
        return 'Verified';
      case 'REJECTED':
        return 'Rejected';
      case 'PENDING':
      default:
        return 'Pending';
    }
  }

  // ========== CLIENT-SPECIFIC METHODS ==========

  loadClientDocuments(): void {
    this.loading = true;
    this.error = null;

    this.documentService
      .getMyDocuments(this.currentPage, this.pageSize)
      .subscribe({
        next: (response) => {
          this.documents = response.content || response || [];
          this.totalItems =
            response.totalElements || response.length || this.documents.length;
          this.loading = false;
        },
        error: (error: any) => {
          this.error = 'Failed to load your documents';
          this.loading = false;
          console.error('Error loading client documents:', error);
        },
      });
  }

  loadClientStatistics(): void {
    this.documentService.getMyDocumentStatistics().subscribe({
      next: (statistics) => {
        this.statistics = statistics;
      },
      error: (error: any) => {
        console.error('Error loading client document statistics:', error);
      },
    });
  }

  searchClientDocuments(): void {
    this.currentPage = 0;
    this.loadClientDocuments();
  }

  applyClientFilters(): void {
    this.currentPage = 0;
    this.loadClientDocumentsWithFilters();
  }

  clearClientFilters(): void {
    this.searchTerm = '';
    this.selectedDocumentType = '';
    this.selectedVerificationStatus = '';
    this.currentPage = 0;
    this.loadClientDocumentsWithFilters();
  }

  loadClientDocumentsWithFilters(): void {
    this.loading = true;
    this.error = null;

    // If search term is provided, use search endpoint
    if (this.searchTerm.trim()) {
      this.documentService.searchMyDocuments(this.searchTerm.trim()).subscribe({
        next: (documents) => {
          this.documents = documents;
          this.totalItems = documents.length;
          this.loading = false;
        },
        error: (error: any) => {
          this.error = 'Failed to search documents';
          this.loading = false;
          console.error('Error searching client documents:', error);
        },
      });
      return;
    }

    // Use the new filtered endpoint for all other cases
    this.documentService
      .getMyDocuments(
        this.currentPage,
        this.pageSize,
        this.selectedDocumentType || undefined,
        this.selectedVerificationStatus || undefined
      )
      .subscribe({
        next: (response) => {
          this.documents = response.content || response || [];
          this.totalItems =
            response.totalElements || response.length || this.documents.length;
          this.loading = false;
        },
        error: (error: any) => {
          this.error = 'Failed to load documents';
          this.loading = false;
          console.error('Error loading client documents:', error);
        },
      });
  }

  onClientPageChange(page: number): void {
    this.currentPage = page - 1;
    if (this.searchTerm.trim()) {
      this.searchClientDocuments();
    } else {
      this.loadClientDocuments();
    }
  }

  // ========== STAFF-SPECIFIC METHODS ==========

  loadStaffDocuments(): void {
    this.loading = true;
    this.error = null;

    const filters: any = {
      page: this.currentPage,
      size: this.pageSize,
    };

    // Include search term in filters if it exists
    if (this.searchTerm.trim()) {
      filters.searchTerm = this.searchTerm.trim();
    }

    // Include other filter parameters
    if (this.selectedClientId) filters.clientId = this.selectedClientId;
    if (this.selectedTaskId) filters.taskId = this.selectedTaskId;
    if (this.selectedDocumentType)
      filters.documentType = this.selectedDocumentType;
    if (this.selectedVerificationStatus)
      filters.status = this.selectedVerificationStatus;

    this.documentService
      .getStaffDocuments(
        filters.page,
        filters.size,
        filters.searchTerm,
        filters.status,
        filters.documentType,
        filters.clientId,
        filters.taskId
      )
      .subscribe({
        next: (response) => {
          this.documents = response.content || [];
          this.totalItems = response.totalElements || 0;
          this.loading = false;
        },
        error: (error: any) => {
          this.error = 'Failed to load your assigned documents';
          this.loading = false;
          console.error('Error loading staff documents:', error);
        },
      });
  }

  loadStaffStatistics(): void {
    this.documentService.getStaffDocumentStatistics().subscribe({
      next: (statistics) => {
        this.statistics = statistics;
      },
      error: (error: any) => {
        console.error('Error loading staff document statistics:', error);
      },
    });
  }

  loadStaffTasks(): void {
    // Load tasks assigned to the current staff member
    this.taskService.getTasks({ page: 0, size: 1000 }).subscribe({
      next: (response: any) => {
        this.tasks = response.content || [];
        this.filteredTasks = [...this.tasks]; // Initialize filtered tasks
      },
      error: (error: any) => {
        console.error('Error loading staff tasks:', error);
      },
    });
  }

  // Add missing client methods
  loadClientTasks(): void {
    if (this.isClient) {
      this.clientService.getMyTasks(0, 1000).subscribe({
        next: (response: any) => {
          this.tasks = response.content || response || [];
          this.filteredTasks = [...this.tasks]; // Initialize filtered tasks
        },
        error: (error: any) => {
          console.error('Error loading client tasks:', error);
        },
      });
    }
  }

  searchStaffDocuments(): void {
    this.currentPage = 0;
    this.loadStaffDocuments();
  }

  applyStaffFilters(): void {
    this.currentPage = 0;
    this.loadStaffDocuments();
  }

  clearStaffFilters(): void {
    this.searchTerm = '';
    this.selectedClientId = null;
    this.selectedTaskId = null;
    this.selectedDocumentType = '';
    this.selectedVerificationStatus = '';
    this.currentPage = 0;
    this.filteredTasks = [...this.tasks]; // Reset filtered tasks to all tasks
    this.loadStaffDocuments();
  }

  onStaffPageChange(page: number): void {
    this.currentPage = page - 1;
    this.loadStaffDocuments();
  }

  onStaffPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadStaffDocuments();
  }

  // Staff document verification methods
  verifyStaffDocument(document: any): void {
    this.documentService.verifyStaffDocument(document.id, true).subscribe({
      next: (updatedDocument) => {
        // Update the document in the list
        const index = this.documents.findIndex((d) => d.id === document.id);
        if (index !== -1) {
          this.documents[index] = updatedDocument;
        }
        this.toastService.showSuccess('Document verified successfully!');
        this.loadStaffStatistics(); // Refresh statistics
      },
      error: (error: any) => {
        console.error('Error verifying document:', error);
        this.toastService.showError(
          'Failed to verify document. Please try again.'
        );
      },
    });
  }

  rejectStaffDocument(document: any): void {
    const rejectionReason = prompt('Please provide a rejection reason:');
    if (rejectionReason && rejectionReason.trim()) {
      this.documentService
        .rejectStaffDocument(document.id, rejectionReason.trim())
        .subscribe({
          next: (updatedDocument) => {
            // Update the document in the list
            const index = this.documents.findIndex((d) => d.id === document.id);
            if (index !== -1) {
              this.documents[index] = updatedDocument;
            }
            this.toastService.showSuccess('Document rejected successfully!');
            this.loadStaffStatistics(); // Refresh statistics
          },
          error: (error: any) => {
            console.error('Error rejecting document:', error);
            this.toastService.showError(
              'Failed to reject document. Please try again.'
            );
          },
        });
    }
  }

  downloadStaffDocument(document: any): void {
    this.documentService.downloadStaffDocument(document.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = document.originalFileName || 'document';
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Error downloading document:', error);
        this.toastService.showError(
          'Failed to download document. Please try again.'
        );
      },
    });
  }

  isFormValid(): boolean {
    const formData = this.uploadForm.value;

    // Check if file is selected
    if (!this.selectedFile) {
      return false;
    }

    // Check if document type is selected
    if (!formData.documentType) {
      return false;
    }

    // Check if task is selected (now mandatory for all users)
    if (!formData.taskId) {
      return false;
    }

    // For admin/staff, clientId is required
    if (!this.isClient && !formData.clientId) {
      return false;
    }

    return true;
  }

  onClientPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    if (this.searchTerm.trim()) {
      this.searchClientDocuments();
    } else {
      this.loadClientDocuments();
    }
  }
}
