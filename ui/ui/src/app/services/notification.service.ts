import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, interval } from 'rxjs';
import { API_CONFIG } from '../api.config';

export interface Notification {
  id: number;
  userId: number;
  title: string;
  message: string;
  notificationType: string;
  isRead: boolean;
  relatedTaskId?: number;
  relatedDocumentId?: number;
  createdAt: string;
  readAt?: string;
  taskTitle?: string;
  documentName?: string;
  senderName?: string;
  senderRole?: string;
}

export interface NotificationResponse {
  content: Notification[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private baseUrl = API_CONFIG.baseUrl;

  // BehaviorSubject to track unread notification count
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  // BehaviorSubject to track latest notifications
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  constructor(private http: HttpClient) {
    // Start polling for notifications every 30 seconds
    this.startNotificationPolling();
  }

  // Get notifications with pagination
  getNotifications(
    page: number = 0,
    size: number = 20
  ): Observable<NotificationResponse> {
    return this.http.get<NotificationResponse>(
      `${this.baseUrl}/api/notifications?page=${page}&size=${size}`
    );
  }

  // Get unread notifications
  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(
      `${this.baseUrl}/api/notifications/unread`
    );
  }

  // Get unread notification count
  getUnreadNotificationCount(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/api/notifications/count`);
  }

  // Mark notifications as read
  markNotificationsAsRead(notificationIds: number[]): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/api/notifications/mark-read`,
      notificationIds
    );
  }

  // Mark all notifications as read
  markAllNotificationsAsRead(): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/api/notifications/mark-all-read`,
      {}
    );
  }

  // Update unread count
  updateUnreadCount(): void {
    this.getUnreadNotificationCount().subscribe({
      next: (count) => {
        this.unreadCountSubject.next(count);
      },
      error: (error) => {
        console.error('Error fetching unread notification count:', error);
      },
    });
  }

  // Update notifications list
  updateNotifications(): void {
    this.getUnreadNotifications().subscribe({
      next: (notifications) => {
        const previousCount = this.notificationsSubject.value.length;
        const newCount = notifications.length;

        // Play notification sound if new notifications arrived
        if (newCount > previousCount) {
          this.playNotificationSound();
        }

        this.notificationsSubject.next(notifications);
      },
      error: (error) => {
        console.error('Error fetching notifications:', error);
      },
    });
  }

  // Play notification sound
  private playNotificationSound(): void {
    try {
      const audio = new Audio(
        'data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBSuBzvLZiTYIG2m98OScTgwOUarm7blmGgU7k9n1unEiBC13yO/eizEIHWq+8+OWT'
      );
      audio.volume = 0.3;
      audio.play().catch(() => {
        // Ignore errors if audio fails to play
      });
    } catch (error) {
      // Ignore errors if audio is not supported
    }
  }

  // Start polling for notifications
  private startNotificationPolling(): void {
    // Initial load
    this.updateUnreadCount();
    this.updateNotifications();

    // Poll every 30 seconds
    interval(30000).subscribe(() => {
      this.updateUnreadCount();
      this.updateNotifications();
    });
  }

  // Mark a single notification as read
  markAsRead(notificationId: number): void {
    this.markNotificationsAsRead([notificationId]).subscribe({
      next: () => {
        // Update the local state
        this.updateUnreadCount();
        this.updateNotifications();
      },
      error: (error) => {
        console.error('Error marking notification as read:', error);
      },
    });
  }

  // Mark all notifications as read
  markAllAsRead(): void {
    this.markAllNotificationsAsRead().subscribe({
      next: () => {
        // Update the local state
        this.updateUnreadCount();
        this.updateNotifications();
      },
      error: (error) => {
        console.error('Error marking all notifications as read:', error);
      },
    });
  }

  // Get notification icon based on type
  getNotificationIcon(type: string): string {
    switch (type) {
      case 'TASK_ASSIGNED':
        return 'assignment';
      case 'STATUS_UPDATE':
        return 'update';
      case 'DOCUMENT_UPLOADED':
        return 'upload_file';
      case 'DOCUMENT_VERIFIED':
        return 'verified';
      case 'DOCUMENT_REJECTED':
        return 'error';
      case 'DEADLINE_REMINDER':
        return 'schedule';
      case 'STAFF_ASSIGNED':
        return 'person_add';
      case 'TASK_COMPLETED':
        return 'task_alt';
      case 'TASK_ACKNOWLEDGED':
        return 'thumb_up';
      case 'MESSAGE_RECEIVED':
        return 'message';
      default:
        return 'notifications';
    }
  }

  // Get notification color based on type
  getNotificationColor(type: string): string {
    switch (type) {
      case 'TASK_ASSIGNED':
      case 'STAFF_ASSIGNED':
        return 'text-blue-600';
      case 'STATUS_UPDATE':
      case 'TASK_COMPLETED':
        return 'text-green-600';
      case 'DOCUMENT_UPLOADED':
      case 'DOCUMENT_VERIFIED':
        return 'text-purple-600';
      case 'DOCUMENT_REJECTED':
        return 'text-red-600';
      case 'DEADLINE_REMINDER':
        return 'text-orange-600';
      case 'MESSAGE_RECEIVED':
        return 'text-indigo-600';
      default:
        return 'text-gray-600';
    }
  }

  // Get notification background color based on type
  getNotificationBgColor(type: string): string {
    switch (type) {
      case 'TASK_ASSIGNED':
      case 'STAFF_ASSIGNED':
        return 'bg-blue-50';
      case 'STATUS_UPDATE':
      case 'TASK_COMPLETED':
        return 'bg-green-50';
      case 'DOCUMENT_UPLOADED':
      case 'DOCUMENT_VERIFIED':
        return 'bg-purple-50';
      case 'DOCUMENT_REJECTED':
        return 'bg-red-50';
      case 'DEADLINE_REMINDER':
        return 'bg-orange-50';
      case 'MESSAGE_RECEIVED':
        return 'bg-indigo-50';
      default:
        return 'bg-gray-50';
    }
  }
}
