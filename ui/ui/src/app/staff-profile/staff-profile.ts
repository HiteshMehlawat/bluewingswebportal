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
import { StaffService, Staff } from '../services/staff.service';
import { ToastService } from '../services/toast.service';

interface StaffProfile {
  id: number;
  userId: number;
  employeeId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: string;
  position: string;
  department: string;
  joiningDate: string;
  salary: number;
  supervisorId: number | null;
  supervisorName?: string;
  supervisorEmail?: string;
  supervisorPhone?: string;
  supervisorEmployeeId?: string;
  isAvailable: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  createdById: number;
  updatedBy: string;
  updatedById: number;
}

@Component({
  selector: 'app-staff-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './staff-profile.html',
  styleUrl: './staff-profile.scss',
})
export class StaffProfileComponent implements OnInit {
  currentUser: StoredUser | null = null;
  staffProfile: StaffProfile | null = null;
  loading = true;
  editing = false;
  saving = false;

  profileForm: FormGroup;

  // Fields that staff can edit
  editableFields = ['firstName', 'lastName', 'phone'];

  // Fields that require admin approval (read-only)
  adminOnlyFields = [
    'email',
    'employeeId',
    'role',
    'position',
    'department',
    'salary',
    'supervisorId',
    'isAvailable',
  ];

  constructor(
    private authService: AuthService,
    private staffService: StaffService,
    private fb: FormBuilder,
    private toastService: ToastService
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s()]+$/)]],
      employeeId: [''],
      role: [''],
      position: [''],
      department: [''],
      salary: [0],
      supervisorId: [null],
      isAvailable: [true],
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;

    this.staffService.getCurrentStaff().subscribe({
      next: (profile) => {
        this.staffProfile = profile;
        this.populateForm();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.staffProfile = null;
        this.loading = false;
      },
    });
  }

  populateForm(): void {
    if (this.staffProfile) {
      this.profileForm.patchValue({
        firstName: this.staffProfile.firstName,
        lastName: this.staffProfile.lastName,
        email: this.staffProfile.email,
        phone: this.staffProfile.phone,
        employeeId: this.staffProfile.employeeId,
        role: this.staffProfile.role,
        position: this.staffProfile.position,
        department: this.staffProfile.department,
        salary: this.staffProfile.salary,
        supervisorId: this.staffProfile.supervisorId,
        isAvailable: this.staffProfile.isAvailable,
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

      // Use the new updateMyProfile method
      this.staffService.updateMyProfile(updatedData).subscribe({
        next: (updatedProfile) => {
          this.staffProfile = updatedProfile;
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

  getStatusColor(isAvailable: boolean): string {
    return isAvailable
      ? 'bg-green-100 text-green-800'
      : 'bg-red-100 text-red-800';
  }

  getRoleDisplay(role: string): string {
    switch (role) {
      case 'ADMIN':
        return 'Administrator';
      case 'STAFF':
        return 'Staff Member';
      default:
        return role;
    }
  }

  getAvailabilityDisplay(isAvailable: boolean): string {
    return isAvailable ? 'Available' : 'Unavailable';
  }

  formatSalary(salary: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
    }).format(salary);
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }
}
