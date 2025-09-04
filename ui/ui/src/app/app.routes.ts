import { Routes } from '@angular/router';
import { Login } from './login/login';
import { authGuard } from './guards/auth.guard';
import { Layout } from './layout/layout';
import { roleGuard } from './guards/role.guard';
import { redirectGuard } from './guards/redirect.guard';

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [redirectGuard] },
  // Public lead form (no authentication required)
  {
    path: 'lead-form',
    loadComponent: () =>
      import('./public-lead-form/public-lead-form.component').then(
        (m) => m.PublicLeadFormComponent
      ),
  },
  // Public landing page
  {
    path: 'landing',
    loadComponent: () =>
      import('./landing-page/landing-page.component').then(
        (m) => m.LandingPageComponent
      ),
  },
  // Public landing page that redirects to lead form
  { path: 'inquiry', redirectTo: '/lead-form', pathMatch: 'full' },
  // Root path - show landing page for public access
  { path: '', redirectTo: '/landing', pathMatch: 'full' },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    component: Layout,
    children: [
      // Admin Routes
      {
        path: 'admin',
        canActivate: [() => roleGuard(['ADMIN'])],
        loadComponent: () =>
          import('./admin-dashboard/admin-dashboard').then(
            (m) => m.AdminDashboard
          ),
      },
      // Staff Dashboard Route
      {
        path: 'staff',
        canActivate: [() => roleGuard(['STAFF'])],
        loadComponent: () =>
          import('./staff-dashboard/staff-dashboard').then(
            (m) => m.StaffDashboardComponent
          ),
      },
      {
        path: 'client-management',
        canActivate: [() => roleGuard(['ADMIN', 'STAFF'])],
        loadComponent: () =>
          import('./client-management/client-management.component').then(
            (m) => m.ClientManagementComponent
          ),
      },
      {
        path: 'lead-management',
        canActivate: [() => roleGuard(['ADMIN'])],
        loadComponent: () =>
          import('./lead-management/lead-management.component').then(
            (m) => m.LeadManagementComponent
          ),
      },
      {
        path: 'my-leads',
        canActivate: [() => roleGuard(['STAFF'])],
        loadComponent: () =>
          import('./lead-management/lead-management.component').then(
            (m) => m.LeadManagementComponent
          ),
      },
      {
        path: 'staff-management',
        canActivate: [() => roleGuard(['ADMIN'])],
        loadComponent: () =>
          import('./staff-management/staff-management.component').then(
            (m) => m.StaffManagementComponent
          ),
      },
      {
        path: 'staff-activities',
        canActivate: [() => roleGuard(['ADMIN', 'STAFF'])],
        loadComponent: () =>
          import('./staff-activities/staff-activities.component').then(
            (m) => m.StaffActivitiesComponent
          ),
      },
      {
        path: 'task-management',
        canActivate: [() => roleGuard(['ADMIN', 'STAFF'])],
        loadComponent: () =>
          import('./task-management/task-management.component').then(
            (m) => m.TaskManagementComponent
          ),
      },
      {
        path: 'document-management',
        canActivate: [() => roleGuard(['ADMIN', 'STAFF'])],
        loadComponent: () =>
          import('./document-management/document-management.component').then(
            (m) => m.DocumentManagementComponent
          ),
      },
      {
        path: 'deadline-management',
        canActivate: [() => roleGuard(['ADMIN', 'STAFF'])],
        loadComponent: () =>
          import('./deadline-management/deadline-management.component').then(
            (m) => m.DeadlineManagementComponent
          ),
      },
      {
        path: 'service-requests',
        canActivate: [() => roleGuard(['ADMIN', 'STAFF', 'CLIENT'])],
        loadComponent: () =>
          import('./service-request/service-request.component').then(
            (m) => m.ServiceRequestComponent
          ),
      },

      // Client Routes
      {
        path: 'client',
        canActivate: [() => roleGuard(['CLIENT'])],
        loadComponent: () =>
          import('./client-dashboard/client-dashboard').then(
            (m) => m.ClientDashboardComponent
          ),
      },
      {
        path: 'my-tasks',
        canActivate: [() => roleGuard(['CLIENT'])],
        loadComponent: () =>
          import('./task-management/task-management.component').then(
            (m) => m.TaskManagementComponent
          ),
      },
      {
        path: 'notifications',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./notifications/notifications.component').then(
            (m) => m.NotificationsComponent
          ),
      },
      {
        path: 'my-documents',
        canActivate: [() => roleGuard(['CLIENT'])],
        loadComponent: () =>
          import('./document-management/document-management.component').then(
            (m) => m.DocumentManagementComponent
          ),
      },
      {
        path: 'my-deadlines',
        canActivate: [() => roleGuard(['CLIENT'])],
        loadComponent: () =>
          import('./client-dashboard/client-dashboard').then(
            (m) => m.ClientDashboardComponent
          ),
      },
      {
        path: 'messages',
        canActivate: [() => roleGuard(['CLIENT'])],
        loadComponent: () =>
          import('./client-dashboard/client-dashboard').then(
            (m) => m.ClientDashboardComponent
          ),
      },
      {
        path: 'profile',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./profile/profile').then((m) => m.ProfileComponent),
      },
      {
        path: 'staff-profile',
        canActivate: [() => roleGuard(['STAFF'])],
        loadComponent: () =>
          import('./staff-profile/staff-profile').then(
            (m) => m.StaffProfileComponent
          ),
      },
    ],
  },
  // Catch all route - redirect to login if no route matches
  { path: '**', redirectTo: '/login' },
];
