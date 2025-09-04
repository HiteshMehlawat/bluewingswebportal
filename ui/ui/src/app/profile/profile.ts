import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { AuthService, StoredUser } from '../services/auth.service';
import { ClientService } from '../services/client.service';
import { ToastService } from '../services/toast.service';

interface ClientProfile {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  companyName: string;
  address: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
  gstNumber?: string;
  panNumber?: string;
  registrationDate: string;
  status: string;
  // Admin-only fields (read-only for clients)
  clientType?: string;
  industry?: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class ProfileComponent implements OnInit {
  currentUser: StoredUser | null = null;
  clientProfile: ClientProfile | null = null;
  loading = true;
  editing = false;
  saving = false;

  profileForm: FormGroup;

  // Fields that clients can edit
  editableFields = [
    'firstName',
    'lastName',
    'phone',
    'address',
    'city',
    'state',
    'pincode',
    'country',
  ];

  // Fields that require admin approval (read-only)
  adminOnlyFields = [
    'email',
    'companyName',
    'gstNumber',
    'panNumber',
    'clientType',
    'industry',
  ];

  constructor(
    private authService: AuthService,
    private clientService: ClientService,
    private fb: FormBuilder,
    private toastService: ToastService
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s()]+$/)]],
      companyName: ['', Validators.required],
      address: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      pincode: [
        '',
        [Validators.required, Validators.pattern(/^[0-9A-Za-z\s\-]+$/)],
      ],
      country: ['', Validators.required],
      gstNumber: [''],
      panNumber: [''],
      clientType: [''],
      industry: [''],
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;

    this.clientService.getMyProfile().subscribe({
      next: (profile) => {
        this.clientProfile = profile;
        this.populateForm();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.clientProfile = null;
        this.loading = false;
      },
    });
  }

  populateForm(): void {
    if (this.clientProfile) {
      this.profileForm.patchValue({
        firstName: this.clientProfile.firstName,
        lastName: this.clientProfile.lastName,
        email: this.clientProfile.email,
        phone: this.clientProfile.phone,
        companyName: this.clientProfile.companyName,
        address: this.clientProfile.address,
        city: this.clientProfile.city,
        state: this.clientProfile.state,
        pincode: this.clientProfile.pincode,
        country: this.clientProfile.country,
        gstNumber: this.clientProfile.gstNumber,
        panNumber: this.clientProfile.panNumber,
        clientType: this.clientProfile.clientType,
        industry: this.clientProfile.industry,
      });
    }
  }

  startEditing(): void {
    this.editing = true;
  }

  cancelEditing(): void {
    this.editing = false;
    this.populateForm(); // Reset form to original values
  }

  saveProfile(): void {
    if (this.profileForm.valid) {
      this.saving = true;

      // Only update editable fields
      const updatedData: { [key: string]: any } = {};
      this.editableFields.forEach((field) => {
        updatedData[field] = this.profileForm.get(field)?.value;
      });

      this.clientService.updateMyProfile(updatedData).subscribe({
        next: (updatedProfile) => {
          this.clientProfile = updatedProfile;
          this.saving = false;
          this.editing = false;
          this.toastService.showSuccess('Profile updated successfully!');
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          this.saving = false;
          this.toastService.showError(
            'Failed to update profile. Please try again.'
          );
        },
      });
    } else {
      this.markFormGroupTouched();
      this.toastService.showError('Please fix the errors in the form');
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.profileForm.controls).forEach((key) => {
      const control = this.profileForm.get(key);
      control?.markAsTouched();
    });
  }

  isFieldEditable(fieldName: string): boolean {
    return this.editing && this.editableFields.includes(fieldName);
  }

  isFieldReadOnly(fieldName: string): boolean {
    return this.adminOnlyFields.includes(fieldName);
  }

  getFieldClass(fieldName: string): string {
    if (this.isFieldReadOnly(fieldName)) {
      return 'bg-gray-100 cursor-not-allowed';
    }
    if (this.isFieldEditable(fieldName)) {
      return 'bg-white border-blue-300 focus:border-blue-500 focus:ring-blue-500';
    }
    return 'bg-gray-50 cursor-not-allowed';
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'INACTIVE':
        return 'bg-red-100 text-red-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getClientTypeDisplay(type: string): string {
    switch (type) {
      case 'CORPORATE':
        return 'Corporate';
      case 'INDIVIDUAL':
        return 'Individual';
      case 'PARTNERSHIP':
        return 'Partnership';
      default:
        return type;
    }
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }
}
