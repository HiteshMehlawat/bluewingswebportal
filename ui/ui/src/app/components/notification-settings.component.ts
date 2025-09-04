import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface NotificationPreference {
  type: string;
  title: string;
  description: string;
  enabled: boolean;
  email: boolean;
  push: boolean;
}

@Component({
  selector: 'app-notification-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="mb-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-2">
          Notification Settings
        </h2>
        <p class="text-gray-600">
          Configure how you want to receive notifications
        </p>
      </div>

      <!-- General Settings -->
      <div class="mb-8">
        <h3 class="text-lg font-medium text-gray-900 mb-4">General Settings</h3>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <div>
              <label class="text-sm font-medium text-gray-700"
                >Enable Notifications</label
              >
              <p class="text-sm text-gray-500">
                Receive notifications in the browser
              </p>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                [(ngModel)]="generalSettings.enabled"
                class="sr-only peer"
              />
              <div
                class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"
              ></div>
            </label>
          </div>

          <div class="flex items-center justify-between">
            <div>
              <label class="text-sm font-medium text-gray-700"
                >Notification Sound</label
              >
              <p class="text-sm text-gray-500">
                Play sound for new notifications
              </p>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                [(ngModel)]="generalSettings.sound"
                class="sr-only peer"
              />
              <div
                class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"
              ></div>
            </label>
          </div>

          <div class="flex items-center justify-between">
            <div>
              <label class="text-sm font-medium text-gray-700"
                >Email Notifications</label
              >
              <p class="text-sm text-gray-500">
                Receive notifications via email
              </p>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                [(ngModel)]="generalSettings.email"
                class="sr-only peer"
              />
              <div
                class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"
              ></div>
            </label>
          </div>
        </div>
      </div>

      <!-- Notification Types -->
      <div class="mb-8">
        <h3 class="text-lg font-medium text-gray-900 mb-4">
          Notification Types
        </h3>
        <div class="space-y-4">
          <div
            *ngFor="let preference of notificationPreferences"
            class="border border-gray-200 rounded-lg p-4"
          >
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center space-x-3 mb-2">
                  <span
                    class="material-icons text-lg"
                    [class]="getNotificationColor(preference.type)"
                  >
                    {{ getNotificationIcon(preference.type) }}
                  </span>
                  <h4 class="text-sm font-medium text-gray-900">
                    {{ preference.title }}
                  </h4>
                </div>
                <p class="text-sm text-gray-500 mb-3">
                  {{ preference.description }}
                </p>

                <div class="flex items-center space-x-6">
                  <label class="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      [(ngModel)]="preference.enabled"
                      class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span class="text-sm text-gray-700">In-app</span>
                  </label>
                  <label class="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      [(ngModel)]="preference.email"
                      [disabled]="!generalSettings.email"
                      class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span
                      class="text-sm text-gray-700"
                      [class.text-gray-400]="!generalSettings.email"
                      >Email</span
                    >
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Save Button -->
      <div class="flex justify-end space-x-3">
        <button
          (click)="resetSettings()"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          Reset
        </button>
        <button
          (click)="saveSettings()"
          class="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          Save Settings
        </button>
      </div>
    </div>
  `,
})
export class NotificationSettingsComponent implements OnInit {
  generalSettings = {
    enabled: true,
    sound: true,
    email: false,
  };

  notificationPreferences: NotificationPreference[] = [
    {
      type: 'TASK_ASSIGNED',
      title: 'Task Assignments',
      description: 'When a new task is assigned to you',
      enabled: true,
      email: false,
      push: true,
    },
    {
      type: 'STATUS_UPDATE',
      title: 'Status Updates',
      description: 'When task status changes',
      enabled: true,
      email: false,
      push: true,
    },
    {
      type: 'DOCUMENT_UPLOADED',
      title: 'Document Uploads',
      description: 'When documents are uploaded',
      enabled: true,
      email: false,
      push: true,
    },
    {
      type: 'DOCUMENT_VERIFIED',
      title: 'Document Verification',
      description: 'When documents are verified',
      enabled: true,
      email: true,
      push: true,
    },
    {
      type: 'DOCUMENT_REJECTED',
      title: 'Document Rejection',
      description: 'When documents are rejected',
      enabled: true,
      email: true,
      push: true,
    },
    {
      type: 'STAFF_ASSIGNED',
      title: 'Staff Assignment',
      description: 'When staff is assigned to your account',
      enabled: true,
      email: true,
      push: true,
    },
    {
      type: 'TASK_COMPLETED',
      title: 'Task Completion',
      description: 'When tasks are completed',
      enabled: true,
      email: true,
      push: true,
    },
    {
      type: 'DEADLINE_REMINDER',
      title: 'Deadline Reminders',
      description: 'Reminders for upcoming deadlines',
      enabled: true,
      email: true,
      push: true,
    },
    {
      type: 'MESSAGE_RECEIVED',
      title: 'Messages',
      description: 'When you receive new messages',
      enabled: true,
      email: false,
      push: true,
    },
  ];

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings(): void {
    // Load settings from localStorage
    const savedSettings = localStorage.getItem('notificationSettings');
    if (savedSettings) {
      const settings = JSON.parse(savedSettings);
      this.generalSettings = { ...this.generalSettings, ...settings.general };
      this.notificationPreferences =
        settings.preferences || this.notificationPreferences;
    }
  }

  saveSettings(): void {
    // Save settings to localStorage
    const settings = {
      general: this.generalSettings,
      preferences: this.notificationPreferences,
    };
    localStorage.setItem('notificationSettings', JSON.stringify(settings));

    // Show success message (you can integrate with your toast service)
    console.log('Notification settings saved successfully!');
  }

  resetSettings(): void {
    // Reset to default settings
    this.generalSettings = {
      enabled: true,
      sound: true,
      email: false,
    };

    this.notificationPreferences.forEach((pref) => {
      pref.enabled = true;
      pref.email =
        pref.type === 'DOCUMENT_VERIFIED' ||
        pref.type === 'DOCUMENT_REJECTED' ||
        pref.type === 'STAFF_ASSIGNED' ||
        pref.type === 'TASK_COMPLETED' ||
        pref.type === 'DEADLINE_REMINDER';
      pref.push = true;
    });
  }

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
      case 'MESSAGE_RECEIVED':
        return 'message';
      default:
        return 'notifications';
    }
  }

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
}
