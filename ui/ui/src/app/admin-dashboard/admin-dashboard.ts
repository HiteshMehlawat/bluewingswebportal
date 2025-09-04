import { Component, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { CommonModule } from '@angular/common';

import {
  DashboardService,
  DashboardStats,
  LatestDocumentUpload,
  UpcomingDeadline,
} from '../services/dashboard.service';
import { DocumentService } from '../services/document.service';
import { FormsModule } from '@angular/forms';
import { PaginationComponent } from '../pagination/pagination.component';
import { FileSizePipe } from '../components/file-size.pipe';
import { API_CONFIG } from '../api.config';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    MatCardModule,
    MatGridListModule,
    MatTableModule,
    MatProgressBarModule,
    MatIconModule,
    MatDividerModule,
    CommonModule,
    FormsModule,
    PaginationComponent,
    FileSizePipe,
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboard implements OnInit {
  totalClients = 0;
  activeCases = 0;
  pendingTasks = 0;
  completedFilings = 0;
  staffPerformanceSummary: any[] = [];
  staffPerformanceSummaryTotal = 0;
  staffPerformanceSummaryPage = 1;
  staffPerformanceSummaryPageSize = 5;
  staffPerformanceSummaryPageSizeOptions = [5, 10, 20];
  latestUploads: LatestDocumentUpload[] = [];
  latestUploadsTotal = 0;
  latestUploadsPage = 1;
  latestUploadsPageSize = 5;
  latestUploadsPageSizeOptions = [5, 10, 20];
  loadingLatestUploads = false;
  upcomingDeadlines: UpcomingDeadline[] = [];
  upcomingDeadlinesTotal = 0;
  upcomingDeadlinesPage = 1;
  upcomingDeadlinesPageSize = 5;
  upcomingDeadlinesPageSizeOptions = [5, 10, 20];
  loadingUpcomingDeadlines = false;
  // Removed clientId as it's not needed for admin dashboard
  totalStaff = 0;

  // Lead statistics
  leadStatistics: any = {};
  recentLeads: any[] = [];

  loading = true;
  error: string | null = null;

  // Pagination state for staffPerformanceSummary
  get staffPerformanceSummaryPaged() {
    const start =
      (this.staffPerformanceSummaryPage - 1) *
      this.staffPerformanceSummaryPageSize;
    return this.staffPerformanceSummary.slice(
      start,
      start + this.staffPerformanceSummaryPageSize
    );
  }
  get staffPerformanceSummaryTotalPages() {
    return Math.ceil(
      this.staffPerformanceSummaryTotal / this.staffPerformanceSummaryPageSize
    );
  }

  // Staff Performance Summary Totals (for current page)
  get staffPerfTotalAssigned() {
    return this.staffPerformanceSummary.reduce(
      (sum, s) => sum + (s.totalAssigned || 0),
      0
    );
  }
  get staffPerfTotalCompleted() {
    return this.staffPerformanceSummary.reduce(
      (sum, s) => sum + (s.completed || 0),
      0
    );
  }
  get staffPerfTotalPending() {
    return this.staffPerformanceSummary.reduce(
      (sum, s) => sum + (s.pending || 0),
      0
    );
  }
  get staffPerfTotalOverdue() {
    return this.staffPerformanceSummary.reduce(
      (sum, s) => sum + (s.overdue || 0),
      0
    );
  }

  // Pagination state for latestUploads
  get latestUploadsPaged() {
    const start = (this.latestUploadsPage - 1) * this.latestUploadsPageSize;
    return this.latestUploads.slice(start, start + this.latestUploadsPageSize);
  }
  get latestUploadsTotalPages() {
    return Math.ceil(this.latestUploads.length / this.latestUploadsPageSize);
  }

  // Pagination state for upcomingDeadlines
  get upcomingDeadlinesPaged() {
    const start =
      (this.upcomingDeadlinesPage - 1) * this.upcomingDeadlinesPageSize;
    return this.upcomingDeadlines.slice(
      start,
      start + this.upcomingDeadlinesPageSize
    );
  }
  get upcomingDeadlinesTotalPages() {
    return Math.ceil(
      this.upcomingDeadlines.length / this.upcomingDeadlinesPageSize
    );
  }

  // Page size options
  getStaffPerformanceSummary() {
    const total = this.staffPerformanceSummaryTotal;
    if (total === 0) return 'No records';
    const start =
      (this.staffPerformanceSummaryPage - 1) *
        this.staffPerformanceSummaryPageSize +
      1;
    const end = Math.min(
      start + this.staffPerformanceSummaryPageSize - 1,
      total
    );
    return `Showing ${start}-${end} out of ${total} records`;
  }
  getLatestUploadsSummary() {
    const total = this.latestUploads.length;
    if (total === 0) return 'No records';
    const start = (this.latestUploadsPage - 1) * this.latestUploadsPageSize + 1;
    const end = Math.min(start + this.latestUploadsPageSize - 1, total);
    return `Showing ${start}-${end} out of ${total} records`;
  }
  getUpcomingDeadlinesSummary() {
    const total = this.upcomingDeadlines.length;
    if (total === 0) return 'No records';
    const start =
      (this.upcomingDeadlinesPage - 1) * this.upcomingDeadlinesPageSize + 1;
    const end = Math.min(start + this.upcomingDeadlinesPageSize - 1, total);
    return `Showing ${start}-${end} of ${total}`;
  }

  loadStaffPerformanceSummary() {
    this.dashboardService
      .getStaffPerformanceSummary(
        this.staffPerformanceSummaryPage - 1,
        this.staffPerformanceSummaryPageSize
      )
      .subscribe((res) => {
        this.staffPerformanceSummary = res.content;
        this.staffPerformanceSummaryTotal = res.totalElements;
      });
  }

  setStaffPerformanceSummaryPage(page: number) {
    this.staffPerformanceSummaryPage = page;
    this.loadStaffPerformanceSummary();
  }
  onStaffPerformanceSummaryPageSizeChange(size: number) {
    this.staffPerformanceSummaryPageSize = size;
    this.staffPerformanceSummaryPage = 1;
    this.loadStaffPerformanceSummary();
  }

  loadLatestUploads() {
    this.loadingLatestUploads = true;
    this.dashboardService
      .getLatestDocumentUploads(
        this.latestUploadsPage - 1,
        this.latestUploadsPageSize
      )
      .subscribe({
        next: (res) => {
          this.latestUploads = res.content;
          this.latestUploadsTotal = res.totalElements;
          this.loadingLatestUploads = false;
        },
        error: () => {
          this.latestUploads = [];
          this.latestUploadsTotal = 0;
          this.loadingLatestUploads = false;
        },
      });
  }
  setLatestUploadsPage(page: number) {
    this.latestUploadsPage = page;
    this.loadLatestUploads();
  }
  onLatestUploadsPageSizeChange(size: number) {
    this.latestUploadsPageSize = size;
    this.latestUploadsPage = 1;
    this.loadLatestUploads();
  }

  loadUpcomingDeadlines() {
    this.loadingUpcomingDeadlines = true;
    this.dashboardService
      .getAdminUpcomingDeadlines(
        this.upcomingDeadlinesPage - 1,
        this.upcomingDeadlinesPageSize
      )
      .subscribe({
        next: (res) => {
          this.upcomingDeadlines = res.content;
          this.upcomingDeadlinesTotal = res.totalElements;
          this.loadingUpcomingDeadlines = false;
        },
        error: () => {
          this.upcomingDeadlines = [];
          this.upcomingDeadlinesTotal = 0;
          this.loadingUpcomingDeadlines = false;
        },
      });
  }
  setUpcomingDeadlinesPage(page: number) {
    this.upcomingDeadlinesPage = page;
    this.loadUpcomingDeadlines();
  }
  onUpcomingDeadlinesPageSizeChange(size: number) {
    this.upcomingDeadlinesPageSize = size;
    this.upcomingDeadlinesPage = 1;
    this.loadUpcomingDeadlines();
  }

  downloadDocument(documentId: number) {
    // Find the document in the latestUploads array to get the original filename
    const docItem = this.latestUploads.find(
      (doc) => doc.documentId === documentId
    );
    const filename = docItem?.originalFileName || `document_${documentId}`;

    this.documentService.downloadDocument(documentId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Error downloading document:', error);
        alert('Failed to download document. Please try again.');
      },
    });
  }

  constructor(
    private dashboardService: DashboardService,
    private documentService: DocumentService
  ) {}

  ngOnInit() {
    this.loadStaffPerformanceSummary();
    this.loadLatestUploads();
    this.loadUpcomingDeadlines();
    this.loadCompletedFilingsCount();
    this.dashboardService.getDashboardStats().subscribe({
      next: (stats: DashboardStats) => {
        this.totalClients = stats.totalClients;
        this.activeCases = stats.activeCases;
        this.pendingTasks = stats.pendingTasks;
        this.completedFilings = stats.completedFilings;
        this.totalStaff = (stats as any).totalStaff ?? 0;
        this.leadStatistics = (stats as any).leadStatistics ?? {};
        this.recentLeads = (stats as any).recentLeads ?? [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load dashboard data.';
        this.loading = false;
      },
    });
  }

  loadCompletedFilingsCount() {
    this.dashboardService.getCompletedFilingsCount().subscribe({
      next: (count) => {
        this.completedFilings = count;
      },
      error: (err) => {
        console.error('Failed to load completed filings count:', err);
        // Keep the default value from dashboard stats
      },
    });
  }
}
