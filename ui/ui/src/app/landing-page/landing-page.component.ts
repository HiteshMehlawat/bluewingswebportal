import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.css'],
})
export class LandingPageComponent {
  services = [
    {
      icon: '📊',
      title: 'ITR Filing',
      description: 'Professional Income Tax Return filing services',
    },
    {
      icon: '🏢',
      title: 'GST Registration',
      description: 'Complete GST registration and compliance',
    },
    {
      icon: '📋',
      title: 'GST Filing',
      description: 'Regular GST return filing and compliance',
    },
    {
      icon: '🏭',
      title: 'Company Registration',
      description: 'Private Limited, LLP, and Partnership registration',
    },
    {
      icon: '💰',
      title: 'TDS Filing',
      description: 'Tax Deducted at Source filing services',
    },
    {
      icon: '🔍',
      title: 'Audit Services',
      description: 'Comprehensive audit and assurance services',
    },
  ];

  features = [
    'Expert Tax Consultants',
    '24/7 Customer Support',
    'Secure Document Handling',
    'Timely Filing Guarantee',
    'Competitive Pricing',
    'Online Tracking System',
  ];
}
