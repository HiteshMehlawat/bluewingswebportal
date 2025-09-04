import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AuthService, StoredUser } from '../services/auth.service';
import {
  StaffDashboardService,
  StaffDashboardStats,
  StaffActivity,
} from '../services/staff-dashboard.service';
import { Task } from '../services/task.service';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-staff-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './staff-dashboard.html',
  styleUrl: './staff-dashboard.scss',
})
export class StaffDashboardComponent implements OnInit {
  currentUser: StoredUser | null = null;
  loading = true;
  stats: StaffDashboardStats | null = null;
  recentTasks: Task[] = [];
  recentActivities: StaffActivity[] = [];

  constructor(
    private authService: AuthService,
    private staffDashboardService: StaffDashboardService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    // Load all dashboard data
    Promise.all([
      this.loadStats(),
      this.loadRecentTasks(),
      this.loadRecentActivities(),
    ]).finally(() => {
      this.loading = false;
    });
  }

  loadStats(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.staffDashboardService.getDashboardStats().subscribe({
        next: (stats) => {
          this.stats = stats;
          resolve();
        },
        error: (error) => {
          console.error('Error loading stats:', error);
          this.stats = null;
          resolve();
        },
      });
    });
  }

  loadRecentTasks(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.staffDashboardService.getRecentTasks(0, 3).subscribe({
        next: (response) => {
          this.recentTasks = response.content || [];
          resolve();
        },
        error: (error) => {
          console.error('Error loading recent tasks:', error);
          this.recentTasks = [];
          resolve();
        },
      });
    });
  }

  loadRecentActivities(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.staffDashboardService.getRecentActivities(0, 5).subscribe({
        next: (response) => {
          this.recentActivities = response.content || [];
          resolve();
        },
        error: (error) => {
          console.error('Error loading recent activities:', error);
          this.recentActivities = [];
          resolve();
        },
      });
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'IN_PROGRESS':
        return 'bg-blue-100 text-blue-800';
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'ON_HOLD':
        return 'bg-gray-100 text-gray-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'HIGH':
      case 'URGENT':
        return 'bg-red-100 text-red-800';
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-800';
      case 'LOW':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getActivityIcon(activity: StaffActivity): string {
    return activity.icon;
  }

  getActivityColor(activity: StaffActivity): string {
    return activity.color;
  }

  formatTimeAgo(timestamp: string): string {
    const date = new Date(timestamp);
    const now = new Date();
    const diffInHours = Math.floor(
      (now.getTime() - date.getTime()) / (1000 * 60 * 60)
    );

    if (diffInHours < 1) {
      return 'Just now';
    } else if (diffInHours < 24) {
      return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    } else {
      const diffInDays = Math.floor(diffInHours / 24);
      return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
    }
  }
}
