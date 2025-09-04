import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { AuthService, StoredUser } from '../services/auth.service';
import { ClientService } from '../services/client.service';
import { TaskService } from '../services/task.service';
import { DocumentService } from '../services/document.service';
import { ToastService } from '../services/toast.service';
import { DateFormatPipe } from '../shared/pipes/date-format.pipe';

interface ClientDashboardStats {
  totalTasks: number;
  pendingTasks: number;
  completedTasks: number;
  overdueTasks: number;
  pendingDocuments: number;
  verifiedDocuments: number;
  upcomingDeadlines: number;
}

interface RecentActivity {
  id: number;
  type:
    | 'TASK_CREATED'
    | 'TASK_UPDATED'
    | 'DOCUMENT_UPLOADED'
    | 'DOCUMENT_VERIFIED'
    | 'DEADLINE_APPROACHING';
  title: string;
  description: string;
  timestamp: string;
  relatedId?: number;
}

interface UpcomingDeadline {
  taskId: number;
  taskName: string; // Changed from taskTitle to match backend
  description: string;
  dueDate: string;
  status: string;
  priority: string;
  taskType: string;
  staffId: number;
  staffName: string;
  latestRemark: string;
  deadlineStatus: string;
  hasDocuments: boolean;
  // Calculated fields
  daysRemaining?: number;
  isOverdue?: boolean;
}

interface AssignedStaff {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: string;
  tasks: StaffTask[];
}

interface StaffTask {
  id: number;
  title: string;
  status: string;
  priority: string;
  dueDate: string;
}

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    DateFormatPipe,
  ],
  templateUrl: './client-dashboard.html',
  styleUrl: './client-dashboard.scss',
})
export class ClientDashboardComponent implements OnInit {
  currentUser: StoredUser | null = null;
  loading = true;
  stats: ClientDashboardStats | null = null;
  recentActivities: RecentActivity[] = [];
  upcomingDeadlines: UpcomingDeadline[] = [];

  // Upload modal
  showUploadModal = false;
  uploadForm: FormGroup;
  selectedFile: File | null = null;
  uploadProgress = 0;
  isUploading = false;
  clientTasks: any[] = []; // Add this for task selection

  // Staff list modal
  showEmailModal = false; // Reusing the same flag
  assignedStaff: AssignedStaff[] = [];
  selectedStaff: AssignedStaff | null = null;

  constructor(
    private authService: AuthService,
    private clientService: ClientService,
    private taskService: TaskService,
    private documentService: DocumentService,
    private fb: FormBuilder,
    private toastService: ToastService,
    private router: Router
  ) {
    this.uploadForm = this.fb.group({
      documentType: ['', Validators.required],
      taskId: ['', Validators.required], // Make taskId required
      file: [null, Validators.required],
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.loadDashboardData();
    this.loadAssignedStaff();
    this.loadClientTasks(); // Add this to load tasks for upload form
  }

  loadDashboardData(): void {
    this.loading = true;

    // Load dashboard statistics
    this.loadStats();

    // Load recent activities
    this.loadRecentActivities();

    // Load upcoming deadlines
    this.loadUpcomingDeadlines();

    this.loading = false;
  }

  loadStats(): void {
    this.clientService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (error) => {
        console.error('Error loading dashboard stats:', error);
        this.stats = null;
      },
    });
  }

  loadRecentActivities(): void {
    this.clientService.getRecentActivities(10).subscribe({
      next: (activities) => {
        this.recentActivities = activities;
      },
      error: (error) => {
        console.error('Error loading recent activities:', error);
        this.recentActivities = [];
      },
    });
  }

  loadUpcomingDeadlines(): void {
    this.clientService.getUpcomingDeadlines().subscribe({
      next: (deadlines) => {
        // Calculate days remaining and overdue status for each deadline
        this.upcomingDeadlines = deadlines.map((deadline) => {
          const dueDate = new Date(deadline.dueDate);
          const today = new Date();
          const timeDiff = dueDate.getTime() - today.getTime();
          const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24));

          return {
            ...deadline,
            daysRemaining: daysDiff,
            isOverdue: daysDiff < 0,
          };
        });
      },
      error: (error) => {
        console.error('Error loading upcoming deadlines:', error);
        this.upcomingDeadlines = [];
      },
    });
  }

  getPriorityBadgeClass(priority: string): string {
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

  // Upload Document Methods
  openUploadModal(): void {
    this.showUploadModal = true;
    this.uploadForm.reset();
    this.selectedFile = null;
    this.loadClientTasks(); // Refresh tasks when modal opens
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
    if (this.uploadForm.valid && this.selectedFile) {
      this.isUploading = true;
      this.uploadProgress = 0;

      const formData = this.uploadForm.value;

      this.documentService
        .uploadMyDocument(
          this.selectedFile,
          formData.documentType,
          formData.taskId // Now taskId is required and will always be present
        )
        .subscribe({
          next: (document) => {
            this.uploadProgress = 100;
            this.closeUploadModal();
            this.loadDashboardData(); // Refresh dashboard data
            this.loadClientTasks(); // Refresh client tasks
            this.toastService.showSuccess('Document uploaded successfully!');
          },
          error: (error: any) => {
            console.error('Error uploading document:', error);
            this.isUploading = false;
            this.toastService.showError(
              'Failed to upload document. Please try again.'
            );
          },
        });
    }
  }

  // Staff List Methods
  openStaffListModal(): void {
    this.showEmailModal = true; // Reusing the same modal flag
  }

  closeStaffListModal(): void {
    this.showEmailModal = false;
    this.selectedStaff = null;
  }

  sendEmailToStaff(staff: AssignedStaff): void {
    if (staff.email) {
      const subject = `Message from client regarding tasks`;
      const body = `Dear ${staff.firstName} ${staff.lastName},

I hope this email finds you well. I am writing regarding the tasks you are handling for me.

Tasks you are working on:
${staff.tasks
  .map((task) => `- ${task.title} (${task.status}, Due: ${task.dueDate})`)
  .join('\n')}

Please let me know if you need any additional information or have any questions.

Best regards,
${this.currentUser?.firstName} ${this.currentUser?.lastName}`;

      const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${
        staff.email
      }&su=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
      window.open(gmailUrl, '_blank');
    } else {
      this.toastService.showError('Staff email not available');
    }
  }

  loadAssignedStaff(): void {
    // Get staff assigned to client's tasks
    this.clientService.getMyTasks(0, 1000).subscribe({
      next: (response: any) => {
        const tasks = response.content || response || [];
        const staffMap = new Map<number, AssignedStaff>();

        tasks.forEach((task: any) => {
          if (task.assignedStaffId && task.assignedStaffName) {
            if (!staffMap.has(task.assignedStaffId)) {
              staffMap.set(task.assignedStaffId, {
                id: task.assignedStaffId,
                firstName: task.assignedStaffName.split(' ')[0] || '',
                lastName:
                  task.assignedStaffName.split(' ').slice(1).join(' ') || '',
                email: task.assignedStaffEmail || '',
                phone: task.assignedStaffPhone || '',
                role: 'STAFF',
                tasks: [],
              });
            }

            // Add task to staff member
            const staff = staffMap.get(task.assignedStaffId);
            if (staff) {
              staff.tasks.push({
                id: task.id,
                title: task.title,
                status: task.status,
                priority: task.priority,
                dueDate: task.dueDate,
              });
            }
          }
        });

        this.assignedStaff = Array.from(staffMap.values());
      },
      error: (error: any) => {
        console.error('Error loading assigned staff:', error);
        this.assignedStaff = [];
      },
    });
  }

  loadClientTasks(): void {
    this.clientService.getMyTasks(0, 1000).subscribe({
      next: (response: any) => {
        this.clientTasks = response.content || response || [];
      },
      error: (error: any) => {
        console.error('Error loading client tasks:', error);
        this.clientTasks = [];
      },
    });
  }

  // Document types for dropdown
  documentTypes = [
    { value: 'PAN_CARD', label: 'PAN Card' },
    { value: 'AADHAR', label: 'Aadhaar Card' },
    { value: 'BANK_STATEMENT', label: 'Bank Statement' },
    { value: 'INVOICE', label: 'Invoice' },
    { value: 'FORM_16', label: 'Form 16' },
    { value: 'GST_RETURN', label: 'GST Return' },
    { value: 'COMPANY_DOCS', label: 'Company Documents' },
    { value: 'OTHER', label: 'Other' },
  ];

  navigateToServiceRequests(): void {
    this.router.navigate(['/dashboard/service-requests']);
  }
}
