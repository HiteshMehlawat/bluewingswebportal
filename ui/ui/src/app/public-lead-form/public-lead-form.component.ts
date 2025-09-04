import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { API_CONFIG } from '../api.config';
import { PhoneInputComponent } from '../components/phone-input.component';
import { ServiceSelectionComponent } from '../shared/service-selection/service-selection.component';

@Component({
  selector: 'app-public-lead-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    PhoneInputComponent,
    ServiceSelectionComponent,
  ],
  templateUrl: './public-lead-form.component.html',
  styleUrls: ['./public-lead-form.component.css'],
})
export class PublicLeadFormComponent {
  leadForm: FormGroup;
  submitting = false;
  submitted = false;
  error = '';

  selectedServiceItem: any = null;

  sourceOptions = [
    { value: 'WEBSITE', label: 'Website' },
    { value: 'SOCIAL_MEDIA', label: 'Social Media' },
    { value: 'REFERRAL', label: 'Referral' },
    { value: 'ADVERTISING', label: 'Advertising' },
    { value: 'OTHER', label: 'Other' },
  ];

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.leadForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required]],
      companyName: [''],
      serviceItemId: ['', Validators.required],
      serviceDescription: [''],
      source: ['WEBSITE'],
    });
  }

  onServiceSelected(serviceItem: any): void {
    this.selectedServiceItem = serviceItem;
    // Set the form control value to just the ID
    this.leadForm.patchValue({ serviceItemId: serviceItem.id });
  }

  onSubmit(): void {
    if (this.leadForm.valid) {
      this.submitting = true;
      this.error = '';

      const formData = this.leadForm.value;

      // Add service hierarchy information to the request
      if (this.selectedServiceItem) {
        formData.serviceCategoryName = this.selectedServiceItem.categoryName;
        formData.serviceSubcategoryName =
          this.selectedServiceItem.subcategoryName;
        formData.serviceItemName = this.selectedServiceItem.name;
      }

      this.http
        .post(`${API_CONFIG.baseUrl}/api/leads/public`, formData)
        .subscribe({
          next: (response) => {
            this.submitted = true;
            this.submitting = false;
            this.leadForm.reset();
            this.leadForm.patchValue({ source: 'WEBSITE' });
            this.selectedServiceItem = null;
          },
          error: (error) => {
            this.error =
              'There was an error submitting your request. Please try again.';
            this.submitting = false;
            console.error('Error submitting lead:', error);
          },
        });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.leadForm.controls).forEach((key) => {
      const control = this.leadForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.leadForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${this.getFieldLabel(fieldName)} is required`;
      }
      if (field.errors['email']) {
        return 'Please enter a valid email address';
      }
      if (field.errors['minlength']) {
        return `${this.getFieldLabel(fieldName)} must be at least ${
          field.errors['minlength'].requiredLength
        } characters`;
      }
      if (field.errors['pattern']) {
        return 'Please enter a valid phone number';
      }
    }
    return '';
  }

  getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      firstName: 'First Name',
      lastName: 'Last Name',
      email: 'Email',
      phone: 'Phone Number',
      companyName: 'Company Name',
      serviceItemId: 'Service Required',
      serviceDescription: 'Service Description',
    };
    return labels[fieldName] || fieldName;
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.leadForm.get(fieldName);
    return !!(field?.invalid && field?.touched);
  }
}
