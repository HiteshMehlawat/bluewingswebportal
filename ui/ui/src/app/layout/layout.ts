import { Component, HostListener } from '@angular/core';
import { Sidebar } from '../sidebar/sidebar';
import {
  RouterOutlet,
  Router,
  NavigationEnd,
  ActivatedRoute,
} from '@angular/router';
import { filter, map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import { NotificationDropdownComponent } from '../components/notification-dropdown.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [Sidebar, RouterOutlet, CommonModule, NotificationDropdownComponent],
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class Layout {
  collapsed = false;
  currentModuleName = 'Admin Dashboard';
  showProfileDropdown = false;

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    this.showProfileDropdown = false;
  }

  onProfileClick(event: MouseEvent) {
    event.stopPropagation();
    this.showProfileDropdown = !this.showProfileDropdown;
  }

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        map(() => {
          let child = this.route.firstChild;
          while (child?.firstChild) {
            child = child.firstChild;
          }
          return (
            child?.snapshot.data['title'] ||
            this.getModuleNameFromUrl(this.router.url)
          );
        })
      )
      .subscribe((name) => {
        // this.currentModuleName = name;
        this.currentModuleName = name;
        // Keep the specific module name instead of generic portal name
      });
  }

  toggleSidebar() {
    this.collapsed = !this.collapsed;
  }

  onLogout() {
    this.auth.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Logout error:', error);
        // Still navigate to login even if logout fails
        this.router.navigate(['/login']);
      },
    });
  }

  navigateToProfile() {
    this.router.navigate(['/dashboard/profile']);
  }

  navigateToStaffProfile() {
    this.router.navigate(['/dashboard/staff-profile']);
  }

  isClient(): boolean {
    const user = this.auth.getUserInfo();
    return user?.role === 'CLIENT';
  }

  isStaff(): boolean {
    const user = this.auth.getUserInfo();
    return user?.role === 'STAFF';
  }
  isAdmin(): boolean {
    const user = this.auth.getUserInfo();
    return user?.role === 'ADMIN';
  }

  getModuleNameFromUrl(url: string): string {
    // Client routes
    if (url.includes('client-dashboard')) return 'Client Dashboard';
    if (url.includes('my-tasks')) return 'My Tasks';
    if (url.includes('my-documents')) return 'My Documents';
    if (url.includes('my-deadlines')) return 'My Deadlines';
    if (url.includes('messages')) return 'Messages';
    if (url.includes('profile')) return 'My Profile';
    if (url.includes('staff-profile')) return 'My Profile';
    if (url.includes('support')) return 'Support';

    // Admin/Staff routes
    if (url.includes('admin-dashboard') || url.includes('/dashboard/admin'))
      return 'Admin Dashboard';
    if (url.includes('staff-dashboard') || url.includes('/dashboard/staff'))
      return 'Staff Dashboard';
    if (url.includes('client-management')) return 'Client Management';
    if (url.includes('staff-management')) return 'Staff Management';
    if (url.includes('staff-activities')) return 'Staff Activities';
    if (url.includes('task-management')) return 'Task Management';
    if (url.includes('document-management')) return 'Document Management';
    if (url.includes('deadline-management')) return 'Deadline Management';
    if (url.includes('service-requests')) return 'Service Requests';

    return 'Dashboard';
  }
}
