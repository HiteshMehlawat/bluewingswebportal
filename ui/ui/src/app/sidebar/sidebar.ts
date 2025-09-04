import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService, StoredUser } from '../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styles: [],
})
export class Sidebar implements OnInit {
  @Input() collapsed = false;
  @Output() toggle = new EventEmitter<void>();

  currentUser: StoredUser | null = null;

  // Staff Management dropdown state
  staffDropdownOpen = false;

  // Client & Lead Management dropdown state
  clientLeadDropdownOpen = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
  }

  toggleSidebar() {
    this.collapsed = !this.collapsed;
    this.toggle.emit();
  }

  toggleStaffDropdown() {
    this.staffDropdownOpen = !this.staffDropdownOpen;
  }

  toggleClientLeadDropdown() {
    this.clientLeadDropdownOpen = !this.clientLeadDropdownOpen;
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  isStaff(): boolean {
    return this.authService.isStaff();
  }

  isClient(): boolean {
    return this.authService.isClient();
  }
}
