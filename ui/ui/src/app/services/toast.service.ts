import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ToastMessage } from '../components/toast-notification.component';

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<ToastMessage[]>([]);
  public toasts$: Observable<ToastMessage[]> =
    this.toastsSubject.asObservable();

  constructor() {}

  showSuccess(message: string, duration: number = 5000): void {
    this.showToast(message, 'success', duration);
  }

  showError(message: string, duration: number = 7000): void {
    this.showToast(message, 'error', duration);
  }

  showWarning(message: string, duration: number = 6000): void {
    this.showToast(message, 'warning', duration);
  }

  showInfo(message: string, duration: number = 5000): void {
    this.showToast(message, 'info', duration);
  }

  private showToast(
    message: string,
    type: 'success' | 'error' | 'warning' | 'info',
    duration: number
  ): void {
    const toast: ToastMessage = {
      id: this.generateId(),
      message,
      type,
      duration,
      timestamp: new Date(),
    };

    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next([...currentToasts, toast]);

    // Auto-remove toast after duration
    if (duration > 0) {
      setTimeout(() => {
        this.removeToast(toast.id);
      }, duration);
    }
  }

  removeToast(id: string): void {
    const currentToasts = this.toastsSubject.value;
    const filteredToasts = currentToasts.filter((toast) => toast.id !== id);
    this.toastsSubject.next(filteredToasts);
  }

  clearAll(): void {
    this.toastsSubject.next([]);
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }
}
