import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ClientService, Client } from '../services/client.service';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../api.config';
import { PhoneInputComponent } from '../components/phone-input.component';
import { CountryDropdownComponent } from '../components/country-dropdown.component';

interface Staff {
  id: number;
  userId: number;
  employeeId: string;
  position: string;
  department: string;
  firstName: string;
  lastName: string;
  email: string;
}

@Component({
  selector: 'app-client-form-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    PhoneInputComponent,
    CountryDropdownComponent,
  ],
  template: `
    <div
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    >
      <div
        class="bg-white rounded-xl shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto"
      >
        <!-- Header -->
        <div class="flex items-center justify-between p-6 border-b">
          <h2 class="text-xl font-semibold text-gray-800">
            {{ isEditMode ? 'Edit Client' : 'Add New Client' }}
          </h2>
          <button (click)="close()" class="text-gray-400 hover:text-gray-600">
            <span class="material-icons text-2xl">close</span>
          </button>
        </div>

        <!-- Form -->
        <form [formGroup]="clientForm" (ngSubmit)="onSubmit()" class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Basic Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-blue-700 border-b pb-2">
                Basic Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >First Name *</label
                >
                <input
                  type="text"
                  formControlName="firstName"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    clientForm.get('firstName')?.invalid &&
                    clientForm.get('firstName')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  First name is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Last Name *</label
                >
                <input
                  type="text"
                  formControlName="lastName"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    clientForm.get('lastName')?.invalid &&
                    clientForm.get('lastName')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Last name is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Email *</label
                >
                <input
                  type="email"
                  formControlName="email"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    clientForm.get('email')?.invalid &&
                    clientForm.get('email')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Valid email is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Phone *</label
                >
                <app-phone-input
                  formControlName="phone"
                  placeholder="Enter phone number"
                  [showError]="
                    (clientForm.get('phone')?.invalid &&
                      clientForm.get('phone')?.touched) ||
                    false
                  "
                  errorMessage="Phone number is required"
                >
                </app-phone-input>
              </div>
            </div>

            <!-- Company Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-red-700 border-b pb-2">
                Company Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Company Name *</label
                >
                <input
                  type="text"
                  formControlName="companyName"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <div
                  *ngIf="
                    clientForm.get('companyName')?.invalid &&
                    clientForm.get('companyName')?.touched
                  "
                  class="text-red-500 text-sm mt-1"
                >
                  Company name is required
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Company Type</label
                >
                <select
                  formControlName="companyType"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Company Type</option>
                  <option value="Private Limited">Private Limited</option>
                  <option value="Public Limited">Public Limited</option>
                  <option value="Partnership">Partnership</option>
                  <option value="Proprietorship">Proprietorship</option>
                  <option value="LLP">LLP</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >GST Number</label
                >
                <input
                  type="text"
                  formControlName="gstNumber"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >PAN Number</label
                >
                <input
                  type="text"
                  formControlName="panNumber"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            <!-- Address Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-pink-700 border-b pb-2">
                Address Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Address</label
                >
                <textarea
                  formControlName="address"
                  rows="3"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                ></textarea>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >City</label
                  >
                  <input
                    type="text"
                    formControlName="city"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >State</label
                  >
                  <input
                    type="text"
                    formControlName="state"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >Pincode</label
                  >
                  <input
                    type="text"
                    formControlName="pincode"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >Country</label
                  >
                  <app-country-dropdown
                    formControlName="country"
                    placeholder="Select a country"
                  >
                  </app-country-dropdown>
                </div>
              </div>
            </div>

            <!-- Business Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-yellow-700 border-b pb-2">
                Business Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Business Type</label
                >
                <select
                  formControlName="businessType"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Business Type</option>
                  <option value="Manufacturing">Manufacturing</option>
                  <option value="Trading">Trading</option>
                  <option value="Services">Services</option>
                  <option value="Retail">Retail</option>
                  <option value="Wholesale">Wholesale</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Industry</label
                >
                <input
                  type="text"
                  formControlName="industry"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Website</label
                >
                <input
                  type="url"
                  formControlName="website"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Client Type</label
                >
                <select
                  formControlName="clientType"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Client Type</option>
                  <option value="INDIVIDUAL">Individual</option>
                  <option value="COMPANY">Company</option>
                  <option value="PARTNERSHIP">Partnership</option>
                  <option value="LLP">LLP</option>
                </select>
              </div>
            </div>

            <!-- Contact Information -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-green-700 border-b pb-2">
                Contact Information
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Contact Person</label
                >
                <input
                  type="text"
                  formControlName="contactPerson"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Contact Phone</label
                >
                <app-phone-input
                  formControlName="contactPhone"
                  placeholder="Enter contact phone number"
                >
                </app-phone-input>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Contact Email</label
                >
                <input
                  type="email"
                  formControlName="contactEmail"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Emergency Contact</label
                >
                <app-phone-input
                  formControlName="emergencyContact"
                  placeholder="Enter emergency contact number"
                >
                </app-phone-input>
              </div>
            </div>

            <!-- Staff Assignment -->
            <div class="space-y-4">
              <h3 class="text-lg font-medium text-purple-700 border-b pb-2">
                Staff Assignment
              </h3>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >Assign Staff Member</label
                >
                <select
                  formControlName="assignedStaffId"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Staff Member (Optional)</option>
                  <option
                    *ngFor="let staff of availableStaff"
                    [value]="staff.id"
                  >
                    {{ staff.firstName }} {{ staff.lastName }} -
                    {{ staff.position }} ({{ staff.employeeId }})
                  </option>
                </select>
                <p class="text-xs text-gray-500 mt-1">
                  You can assign a staff member now or do it later
                </p>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex justify-end gap-4 mt-8 pt-6 border-t">
            <button
              type="button"
              (click)="close()"
              class="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              [disabled]="clientForm.invalid || loading"
              class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span *ngIf="loading" class="inline-block animate-spin mr-2"
                >⟳</span
              >
              {{ isEditMode ? 'Update Client' : 'Add Client' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class ClientFormModalComponent implements OnInit {
  @Input() client: Client | null = null;
  @Output() saved = new EventEmitter<Client>();
  @Output() cancelled = new EventEmitter<void>();

  clientForm: FormGroup;
  loading = false;
  isEditMode = false;
  availableStaff: Staff[] = []; // This would be populated from a staff service

  constructor(
    private fb: FormBuilder,
    private clientService: ClientService,
    private http: HttpClient
  ) {
    this.clientForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      companyName: ['', Validators.required],
      companyType: [''],
      gstNumber: [''],
      panNumber: [''],
      address: [''],
      city: [''],
      state: [''],
      pincode: [''],
      country: [''],
      businessType: [''],
      industry: [''],
      website: [''],
      contactPerson: [''],
      contactPhone: [''],
      contactEmail: [''],
      emergencyContact: [''],
      clientType: ['INDIVIDUAL'],
      registrationDate: [new Date()],
      assignedStaffId: [null],
    });
  }

  ngOnInit(): void {
    this.isEditMode = !!this.client;
    if (this.client) {
      this.clientForm.patchValue(this.client);
    }
    this.loadStaff();
  }

  loadStaff(): void {
    this.http
      .get<Staff[]>(`${API_CONFIG.baseUrl}${API_CONFIG.staff}`)
      .subscribe({
        next: (staff) => {
          this.availableStaff = staff;
        },
        error: (error) => {
          console.error('Error loading staff:', error);
          this.availableStaff = [];
        },
      });
  }

  onSubmit(): void {
    if (this.clientForm.valid) {
      this.loading = true;
      const formData = this.clientForm.value;

      if (this.isEditMode && this.client) {
        // Update existing client
        this.clientService.updateClient(this.client.id, formData).subscribe({
          next: (updatedClient) => {
            this.loading = false;

            this.saved.emit(updatedClient);
          },
          error: (error) => {
            this.loading = false;
            console.error('Error updating client:', error);
            // Show more specific error message
            if (error.error && error.error.details) {
              alert('Error updating client: ' + error.error.details);
            } else {
              alert(
                'Error updating client. Please check the form data and try again.'
              );
            }
          },
        });
      } else {
        // Create new client
        this.clientService.createClient(formData).subscribe({
          next: (newClient) => {
            this.loading = false;

            // If staff is assigned, assign them to the client
            if (formData.assignedStaffId) {
              this.clientService
                .assignStaffToClient(newClient.id, formData.assignedStaffId)
                .subscribe({
                  next: () => {
                    // Staff assigned successfully
                  },
                  error: (error) => {
                    console.error('Error assigning staff:', error);
                    // Staff assignment failed, but client was created successfully
                  },
                });
            }
            this.saved.emit(newClient);
          },
          error: (error) => {
            this.loading = false;
            console.error('Error creating client:', error);
            // Show more specific error message
            if (error.error && error.error.details) {
              alert('Error creating client: ' + error.error.details);
            } else {
              alert(
                'Error creating client. Please check the form data and try again.'
              );
            }
          },
        });
      }
    }
  }

  close(): void {
    this.cancelled.emit();
  }
}
