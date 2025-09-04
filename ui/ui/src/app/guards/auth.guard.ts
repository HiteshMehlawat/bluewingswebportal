import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export const authGuard: CanActivateFn = (route, state): Observable<boolean> => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // First check if user is logged in
  if (auth.isLoggedIn()) {
    return of(true);
  }

  // If not logged in, try to refresh token
  return auth.attemptTokenRefresh().pipe(
    map((refreshSuccess) => {
      if (refreshSuccess) {
        return true;
      } else {
        // Token refresh failed, logout and redirect to login
        auth.logout();
        router.navigate(['/login']);
        return false;
      }
    }),
    catchError(() => {
      // Error during refresh, logout and redirect to login
      auth.logout();
      router.navigate(['/login']);
      return of(false);
    })
  );
};
