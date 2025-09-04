import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Lead {
  id: number;
  leadId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  companyName?: string;
  serviceItemId?: number;
  serviceCategoryName?: string;
  serviceSubcategoryName?: string;
  serviceItemName?: string;
  serviceDescription?: string;
  source: string;
  status: string;
  priority: string;
  assignedStaffId?: number;
  assignedStaffName?: string;
  assignedStaffEmail?: string;
  assignedStaffPhone?: string;
  estimatedValue?: number;
  notes?: string;
  nextFollowUpDate?: string;
  lastContactDate?: string;
  convertedDate?: string;
  lostReason?: string;
  createdAt: string;
  updatedAt: string;
  createdByName?: string;
  updatedByName?: string;
}

@Component({
  selector: 'app-lead-detail-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    >
      <div
        class="bg-white rounded-xl shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto"
      >
        <!-- Header -->
        <div class="flex items-center justify-between p-6 border-b">
          <h2 class="text-xl font-semibold text-gray-800">Lead Details</h2>
          <button (click)="close()" class="text-gray-400 hover:text-gray-600">
            <span class="material-icons text-2xl">close</span>
          </button>
        </div>

        <!-- Content -->
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Basic Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-blue-700 border-b pb-2">
                Basic Information
              </h3>
              <div class="space-y-3">
                <div>
                  <span class="font-semibold">Lead ID:</span> {{ lead?.leadId }}
                </div>
                <div>
                  <span class="font-semibold">Name:</span>
                  {{ lead?.firstName }} {{ lead?.lastName }}
                </div>
                <div>
                  <span class="font-semibold">Email:</span> {{ lead?.email }}
                </div>
                <div>
                  <span class="font-semibold">Phone:</span>
                  {{ lead?.phone || 'N/A' }}
                </div>
                <div>
                  <span class="font-semibold">Company Name:</span>
                  {{ lead?.companyName || 'N/A' }}
                </div>
              </div>
            </div>

            <!-- Lead Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-red-700 border-b pb-2">
                Lead Information
              </h3>
              <div class="space-y-3">
                <div>
                  <span class="font-semibold">Service Information:</span>
                  <div class="mt-1 ml-4">
                    <div *ngIf="lead?.serviceCategoryName">
                      <span class="text-gray-600">Category:</span>
                      {{ lead?.serviceCategoryName }}
                    </div>
                    <div *ngIf="lead?.serviceSubcategoryName">
                      <span class="text-gray-600">Subcategory:</span>
                      {{ lead?.serviceSubcategoryName }}
                    </div>
                    <div *ngIf="lead?.serviceItemName">
                      <span class="text-gray-600">Service:</span>
                      {{ lead?.serviceItemName }}
                    </div>
                    <div
                      *ngIf="
                        !lead?.serviceCategoryName &&
                        !lead?.serviceSubcategoryName &&
                        !lead?.serviceItemName
                      "
                    >
                      <span class="text-gray-500">Not specified</span>
                    </div>
                  </div>
                </div>
                <div *ngIf="lead?.serviceDescription">
                  <span class="font-semibold">Service Description:</span>
                  {{ lead?.serviceDescription }}
                </div>
                <div>
                  <span class="font-semibold">Source:</span>
                  {{ getSourceDisplayName(lead?.source) }}
                </div>
                <div *ngIf="lead?.estimatedValue">
                  <span class="font-semibold">Estimated Value:</span> ₹{{
                    lead?.estimatedValue?.toLocaleString()
                  }}
                </div>
              </div>
            </div>

            <!-- Status and Priority -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-pink-700 border-b pb-2">
                Status & Priority
              </h3>
              <div class="space-y-3">
                <div>
                  <span class="font-semibold">Status:</span>
                  <span
                    class="inline-flex px-2 py-1 text-xs font-semibold rounded-full ml-2"
                    [class]="getStatusBadgeClass(lead?.status)"
                  >
                    {{ getStatusDisplayName(lead?.status) }}
                  </span>
                </div>
                <div>
                  <span class="font-semibold">Priority:</span>
                  <span
                    class="inline-flex px-2 py-1 text-xs font-semibold rounded-full ml-2"
                    [class]="getPriorityBadgeClass(lead?.priority)"
                  >
                    {{ getPriorityDisplayName(lead?.priority) }}
                  </span>
                </div>
                <div *ngIf="lead?.nextFollowUpDate">
                  <span class="font-semibold">Next Follow-up Date:</span>
                  {{ formatDate(lead?.nextFollowUpDate) }}
                </div>
                <div *ngIf="lead?.lastContactDate">
                  <span class="font-semibold">Last Contact Date:</span>
                  {{ formatDate(lead?.lastContactDate) }}
                </div>
                <div *ngIf="lead?.convertedDate">
                  <span class="font-semibold">Converted Date:</span>
                  {{ formatDate(lead?.convertedDate) }}
                </div>
                <div *ngIf="lead?.lostReason">
                  <span class="font-semibold">Lost Reason:</span>
                  {{ lead?.lostReason }}
                </div>
              </div>
            </div>

            <!-- Staff Assignment -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-purple-700 border-b pb-2">
                Staff Assignment
              </h3>
              <div class="space-y-3">
                <div>
                  <span class="font-semibold">Assigned Staff:</span>
                  {{ lead?.assignedStaffName || 'Not Assigned' }}
                </div>
                <div *ngIf="lead?.assignedStaffId">
                  <span class="font-semibold">Staff ID:</span>
                  {{ lead?.assignedStaffId }}
                </div>
                <div *ngIf="lead?.assignedStaffEmail">
                  <span class="font-semibold">Staff Email:</span>
                  {{ lead?.assignedStaffEmail }}
                </div>
                <div *ngIf="lead?.assignedStaffPhone">
                  <span class="font-semibold">Staff Phone:</span>
                  {{ lead?.assignedStaffPhone }}
                </div>
              </div>
            </div>

            <!-- Notes -->
            <div class="space-y-4 md:col-span-2">
              <h3 class="text-lg font-medium text-green-700 border-b pb-2">
                Notes
              </h3>
              <div class="space-y-3">
                <div>
                  <span class="font-semibold">Additional Notes:</span>
                  {{ lead?.notes || 'No notes available' }}
                </div>
              </div>
            </div>

            <!-- Audit Information -->
            <div class="space-y-4 md:col-span-2">
              <h3 class="text-lg font-medium text-gray-700 border-b pb-2">
                Audit Information
              </h3>
              <div class="space-y-3">
                <div>
                  <span class="font-semibold">Created By:</span>
                  {{ lead?.createdByName || 'System' }}
                </div>
                <div>
                  <span class="font-semibold">Created At:</span>
                  {{ formatDateTime(lead?.createdAt) }}
                </div>
                <div>
                  <span class="font-semibold">Updated By:</span>
                  {{ lead?.updatedByName || 'N/A' }}
                </div>
                <div>
                  <span class="font-semibold">Last Updated:</span>
                  {{ formatDateTime(lead?.updatedAt) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex justify-end gap-4 p-6 border-t">
          <button
            type="button"
            (click)="close()"
            class="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  `,
})
export class LeadDetailModalComponent implements OnInit {
  @Input() lead: Lead | null = null;
  @Output() closed = new EventEmitter<void>();

  constructor() {}

  ngOnInit(): void {}

  close(): void {
    this.closed.emit();
  }

  getSourceDisplayName(source: string | undefined): string {
    if (!source) return 'N/A';
    const sourceMap: { [key: string]: string } = {
      WEBSITE: 'Website',
      REFERRAL: 'Referral',
      SOCIAL_MEDIA: 'Social Media',
      COLD_CALL: 'Cold Call',
      EMAIL: 'Email',
      OTHER: 'Other',
    };

    return sourceMap[source] || source;
  }

  getStatusDisplayName(status: string | undefined): string {
    if (!status) return 'N/A';
    const statusMap: { [key: string]: string } = {
      NEW: 'New',
      CONTACTED: 'Contacted',
      IN_DISCUSSION: 'In Discussion',
      PROPOSAL_SENT: 'Proposal Sent',
      CONVERTED: 'Converted',
      LOST: 'Lost',
    };

    return statusMap[status] || status;
  }

  getPriorityDisplayName(priority: string | undefined): string {
    if (!priority) return 'N/A';
    const priorityMap: { [key: string]: string } = {
      LOW: 'Low',
      MEDIUM: 'Medium',
      HIGH: 'High',
      URGENT: 'Urgent',
    };

    return priorityMap[priority] || priority;
  }

  getStatusBadgeClass(status: string | undefined): string {
    if (!status) return 'bg-gray-100 text-gray-800';
    switch (status) {
      case 'NEW':
        return 'bg-blue-100 text-blue-800';
      case 'CONTACTED':
        return 'bg-yellow-100 text-yellow-800';
      case 'IN_DISCUSSION':
        return 'bg-purple-100 text-purple-800';
      case 'PROPOSAL_SENT':
        return 'bg-orange-100 text-orange-800';
      case 'CONVERTED':
        return 'bg-green-100 text-green-800';
      case 'LOST':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getPriorityBadgeClass(priority: string | undefined): string {
    if (!priority) return 'bg-gray-100 text-gray-800';
    switch (priority) {
      case 'LOW':
        return 'bg-gray-100 text-gray-800';
      case 'MEDIUM':
        return 'bg-blue-100 text-blue-800';
      case 'HIGH':
        return 'bg-orange-100 text-orange-800';
      case 'URGENT':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString();
  }

  formatDateTime(dateString: string | undefined): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  }
}
