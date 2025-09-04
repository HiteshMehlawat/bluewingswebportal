import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const userRole = authService.getUserRole();

    if (!userRole) {
      router.navigate(['/login']);
      return false;
    }

    // If user has the required role, allow access
    if (allowedRoles.includes(userRole)) {
      return true;
    }

    // If user doesn't have the required role, redirect to their appropriate dashboard
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
        router.navigate(['/login']);
        break;
    }

    return false;
  };
};
