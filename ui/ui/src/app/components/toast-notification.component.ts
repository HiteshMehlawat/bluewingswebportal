import {
  Component,
  Input,
  OnInit,
  OnDestroy,
  ChangeDetectorRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../services/toast.service';
import { Subscription } from 'rxjs';

export interface ToastMessage {
  id: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
  timestamp: Date;
}

@Component({
  selector: 'app-toast-notification',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.Default,
  template: `
    <div class="fixed bottom-4 right-4 z-50 space-y-2 max-w-sm sm:max-w-md">
      <div
        *ngFor="let toast of toasts"
        class="min-w-72 sm:min-w-80 max-w-sm sm:max-w-md w-auto bg-white shadow-lg rounded-lg pointer-events-auto ring-1 ring-black ring-opacity-5 overflow-hidden"
        [ngClass]="getToastClasses(toast.type)"
      >
        <div class="p-5">
          <div class="flex items-start gap-3">
            <div class="flex-shrink-0">
              <span
                class="material-icons text-xl"
                [ngClass]="getIconClasses(toast.type)"
              >
                {{ getIcon(toast.type) }}
              </span>
            </div>
            <div class="flex-1 pt-0.5 min-w-0">
              <p
                class="text-sm sm:text-base font-medium text-gray-900 break-words leading-relaxed"
              >
                {{ toast.message }}
              </p>
            </div>
            <div class="flex-shrink-0 flex">
              <button
                (click)="removeToast(toast.id)"
                class="bg-white rounded-md inline-flex text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 p-1"
              >
                <span class="sr-only">Close</span>
                <span class="material-icons text-base">close</span>
              </button>
            </div>
          </div>
        </div>
        <!-- Progress bar - static to avoid change detection issues -->
        <div class="h-1.5 bg-gray-200">
          <div
            class="h-1.5"
            [ngClass]="getProgressBarClasses(toast.type)"
            style="width: 100%"
          ></div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
      }
    `,
  ],
})
export class ToastNotificationComponent implements OnInit, OnDestroy {
  toasts: ToastMessage[] = [];
  private subscription: Subscription = new Subscription();
  private progressIntervals: { [key: string]: any } = {};

  constructor(
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.subscription = this.toastService.toasts$.subscribe((toasts) => {
      this.toasts = toasts;
      this.manageProgressBars();
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
    // Clear all timeouts
    Object.values(this.progressIntervals).forEach((timeout) => {
      if (timeout) clearTimeout(timeout);
    });
  }

  removeToast(id: string): void {
    this.toastService.removeToast(id);
  }

  getToastClasses(type: string): string {
    const baseClasses = 'border-l-4';
    switch (type) {
      case 'success':
        return `${baseClasses} border-green-400 bg-green-50`;
      case 'error':
        return `${baseClasses} border-red-400 bg-red-50`;
      case 'warning':
        return `${baseClasses} border-yellow-400 bg-yellow-50`;
      case 'info':
        return `${baseClasses} border-blue-400 bg-blue-50`;
      default:
        return `${baseClasses} border-gray-400 bg-gray-50`;
    }
  }

  getIconClasses(type: string): string {
    switch (type) {
      case 'success':
        return 'text-green-400';
      case 'error':
        return 'text-red-400';
      case 'warning':
        return 'text-yellow-400';
      case 'info':
        return 'text-blue-400';
      default:
        return 'text-gray-400';
    }
  }

  getProgressBarClasses(type: string): string {
    switch (type) {
      case 'success':
        return 'bg-green-400';
      case 'error':
        return 'bg-red-400';
      case 'warning':
        return 'bg-yellow-400';
      case 'info':
        return 'bg-blue-400';
      default:
        return 'bg-gray-400';
    }
  }

  getIcon(type: string): string {
    switch (type) {
      case 'success':
        return 'check_circle';
      case 'error':
        return 'error';
      case 'warning':
        return 'warning';
      case 'info':
        return 'info';
      default:
        return 'info';
    }
  }

  private manageProgressBars(): void {
    // Clear existing intervals
    Object.values(this.progressIntervals).forEach((interval) => {
      if (interval) clearInterval(interval);
    });
    this.progressIntervals = {};

    // Set up new intervals for each toast to auto-remove after duration
    this.toasts.forEach((toast) => {
      if (toast.duration && toast.duration > 0) {
        this.progressIntervals[toast.id] = setTimeout(() => {
          this.toastService.removeToast(toast.id);
          if (this.progressIntervals[toast.id]) {
            delete this.progressIntervals[toast.id];
          }
        }, toast.duration);
      }
    });
  }
}
