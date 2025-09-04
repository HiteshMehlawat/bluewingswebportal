import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationService, Notification } from '../services/notification.service';

@Component({
  selector: 'app-notification-dropdown',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="relative">
      <!-- Notification Bell Icon -->
      <button
        (click)="toggleDropdown()"
        class="relative p-2 text-gray-600 hover:text-gray-900 focus:outline-none focus:text-gray-900"
        [class.text-blue-600]="hasUnreadNotifications"
      >
        <span class="material-icons text-xl">notifications</span>
        
        <!-- Unread Badge -->
        <span
          *ngIf="unreadCount > 0"
          class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center"
        >
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </button>

      <!-- Dropdown Menu -->
      <div
        *ngIf="isDropdownOpen"
        class="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg border border-gray-200 z-50 max-h-96 overflow-y-auto"
      >
        <!-- Header -->
        <div class="flex items-center justify-between p-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-gray-900">Notifications</h3>
          <button
            *ngIf="unreadCount > 0"
            (click)="markAllAsRead()"
            class="text-sm text-blue-600 hover:text-blue-800"
          >
            Mark all as read
          </button>
        </div>

        <!-- Notifications List -->
        <div class="p-2">
          <div
            *ngIf="notifications.length === 0"
            class="text-center py-8 text-gray-500"
          >
            <span class="material-icons text-4xl text-gray-300 mb-2">notifications_off</span>
            <p>No notifications</p>
          </div>

          <div
            *ngFor="let notification of notifications"
            (click)="handleNotificationClick(notification)"
            class="p-3 rounded-lg cursor-pointer transition-colors duration-200 hover:bg-gray-50"
            [class.bg-blue-50]="!notification.isRead"
            [class.border-l-4]="!notification.isRead"
            [class.border-blue-500]="!notification.isRead"
          >
            <!-- Notification Icon -->
            <div class="flex items-start space-x-3">
              <div
                class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center"
                [class]="getNotificationBgColor(notification.notificationType)"
              >
                <span
                  class="material-icons text-sm"
                  [class]="getNotificationColor(notification.notificationType)"
                >
                  {{ getNotificationIcon(notification.notificationType) }}
                </span>
              </div>

              <!-- Notification Content -->
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between">
                  <p
                    class="text-sm font-medium text-gray-900"
                    [class.font-semibold]="!notification.isRead"
                  >
                    {{ notification.title }}
                  </p>
                  <span class="text-xs text-gray-400 ml-2">
                    {{ getTimeAgo(notification.createdAt) }}
                  </span>
                </div>
                <p class="text-sm text-gray-600 mt-1 line-clamp-2">
                  {{ notification.message }}
                </p>
                
                <!-- Related Info -->
                <div *ngIf="notification.taskTitle || notification.documentName" class="mt-2 text-xs text-gray-500">
                  <span *ngIf="notification.taskTitle">
                    Task: {{ notification.taskTitle }}
                  </span>
                  <span *ngIf="notification.documentName">
                    Document: {{ notification.documentName }}
                  </span>
                </div>
              </div>

              <!-- Mark as Read Button -->
              <button
                *ngIf="!notification.isRead"
                (click)="markAsRead(notification.id, $event)"
                class="flex-shrink-0 text-gray-400 hover:text-gray-600"
                title="Mark as read"
              >
                <span class="material-icons text-sm">check</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="p-3 border-t border-gray-200">
          <button
            (click)="viewAllNotifications()"
            class="w-full text-center text-sm text-blue-600 hover:text-blue-800 font-medium"
          >
            View all notifications
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .line-clamp-2 {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  `]
})
export class NotificationDropdownComponent implements OnInit, OnDestroy {
  isDropdownOpen = false;
  notifications: Notification[] = [];
  unreadCount = 0;
  hasUnreadNotifications = false;
  
  private subscriptions: Subscription[] = [];

  constructor(
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to unread count changes
    this.subscriptions.push(
      this.notificationService.unreadCount$.subscribe(count => {
        this.unreadCount = count;
        this.hasUnreadNotifications = count > 0;
      })
    );

    // Subscribe to notifications changes
    this.subscriptions.push(
      this.notificationService.notifications$.subscribe(notifications => {
        this.notifications = notifications;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
    if (this.isDropdownOpen) {
      // Update notifications when dropdown opens
      this.notificationService.updateNotifications();
    }
  }

  markAsRead(notificationId: number, event: Event): void {
    event.stopPropagation();
    this.notificationService.markAsRead(notificationId);
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead();
  }

  handleNotificationClick(notification: Notification): void {
    // Mark as read if unread
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id);
    }

    // Navigate based on notification type
    if (notification.relatedTaskId) {
      this.router.navigate(['/dashboard/task-management'], { 
        queryParams: { taskId: notification.relatedTaskId } 
      });
    } else if (notification.relatedDocumentId) {
      this.router.navigate(['/dashboard/document-management'], { 
        queryParams: { documentId: notification.relatedDocumentId } 
      });
    }

    // Close dropdown
    this.isDropdownOpen = false;
  }

  viewAllNotifications(): void {
    this.router.navigate(['/dashboard/notifications']);
    this.isDropdownOpen = false;
  }

  getNotificationIcon(type: string): string {
    return this.notificationService.getNotificationIcon(type);
  }

  getNotificationColor(type: string): string {
    return this.notificationService.getNotificationColor(type);
  }

  getNotificationBgColor(type: string): string {
    return this.notificationService.getNotificationBgColor(type);
  }

  getTimeAgo(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 60) {
      return 'Just now';
    } else if (diffInSeconds < 3600) {
      const minutes = Math.floor(diffInSeconds / 60);
      return `${minutes}m ago`;
    } else if (diffInSeconds < 86400) {
      const hours = Math.floor(diffInSeconds / 3600);
      return `${hours}h ago`;
    } else {
      const days = Math.floor(diffInSeconds / 86400);
      return `${days}d ago`;
    }
  }
}
