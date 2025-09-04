import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AppInitService {
  constructor(private authService: AuthService, private router: Router) {}

  // Initialize the application
  initializeApp(): Observable<boolean> {
    console.log('Initializing application...');

    // Check if user has stored tokens
    const hasStoredTokens = this.authService.getRefreshToken() !== null;

    if (!hasStoredTokens) {
      console.log('No stored tokens found');
      return of(true);
    }

    // Validate and refresh tokens if needed
    return this.authService.validateAndRefreshTokens().pipe(
      map((isValid) => {
        if (isValid) {
          console.log('Tokens validated successfully');
          return true;
        } else {
          console.log('Token validation failed, clearing auth data');
          this.authService.clearAuthData();
          return true;
        }
      }),
      catchError((error) => {
        console.error('Error during app initialization:', error);
        this.authService.clearAuthData();
        return of(true);
      })
    );
  }

  // Handle authentication state changes
  handleAuthStateChange(): void {
    this.authService.user$.subscribe((user) => {
      if (!user) {
        // User is logged out, redirect to login if not already on a public route
        if (
          !this.router.url.includes('/login') &&
          !this.router.url.includes('/landing') &&
          !this.router.url.includes('/lead-form') &&
          !this.router.url.includes('/inquiry')
        ) {
          this.router.navigate(['/login']);
        }
      }
    });
  }
}
