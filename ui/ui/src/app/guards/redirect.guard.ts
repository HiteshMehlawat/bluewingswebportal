import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const redirectGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const userRole = authService.getUserRole();

  if (!userRole) {
    return true; // Allow access to login page
  }

  // Redirect based on user role
  switch (userRole) {
    case 'ADMIN':
      router.navigate(['/dashboard/admin']);
      break;
    case 'STAFF':
      router.navigate(['/dashboard/staff']);
      break;
    case 'CLIENT':
      router.navigate(['/dashboard/client']);
      break;
    default:
      return true; // Allow access to login page
  }

  return false;
};
