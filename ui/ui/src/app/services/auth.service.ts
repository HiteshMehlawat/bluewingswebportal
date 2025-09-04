import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, throwError, of, timer } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { API_CONFIG } from '../api.config';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'STAFF' | 'CLIENT';
  phone?: string;
  isActive: boolean;
  emailVerified: boolean;
  createdAt: string;
  updatedAt: string;
  lastLogin?: string;
}

export interface StoredUser extends User {
  accessToken: string;
  refreshToken: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RefreshResponse {
  accessToken: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = API_CONFIG.baseUrl;
  private userSubject = new BehaviorSubject<StoredUser | null>(
    this.getUserInfo()
  );
  user$ = this.userSubject.asObservable();
  private refreshTimer: any = null;

  constructor(private http: HttpClient) {
    // Initialize token refresh on service creation only if user is logged in
    const hasTokens = this.getRefreshToken() !== null;
    if (hasTokens) {
      this.initializeTokenRefresh();
      this.handlePageVisibilityChange();
      this.handleBeforeUnload();
    }
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.baseUrl}${API_CONFIG.auth.login}`, {
        email,
        password,
      })
      .pipe(
        tap((res) => {
          const currentUser: StoredUser = {
            ...res.user,
            accessToken: res.accessToken,
            refreshToken: res.refreshToken,
          };
          localStorage.setItem('currentUser', JSON.stringify(currentUser));
          this.userSubject.next(currentUser);
        }),
        catchError((error) => {
          console.error('Login error:', error);
          return throwError(() => error);
        })
      );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}${API_CONFIG.auth.logout}`, {}).pipe(
      tap(() => {
        this.clearAuthData();
      }),
      catchError((error) => {
        console.error('Logout error:', error);
        // Still clear local storage even if backend call fails
        this.clearAuthData();
        return throwError(() => error);
      })
    );
  }

  getAccessToken(): string | null {
    const user = this.getUserInfo();
    return user?.accessToken || null;
  }

  getRefreshToken(): string | null {
    const user = this.getUserInfo();
    return user?.refreshToken || null;
  }

  getUserInfo(): StoredUser | null {
    const user = localStorage.getItem('currentUser');
    return user ? JSON.parse(user) : null;
  }

  isLoggedIn(): boolean {
    const token = this.getAccessToken();
    const refreshToken = this.getRefreshToken();

    // If no tokens at all, user is not logged in
    if (!token && !refreshToken) {
      return false;
    }

    // If access token is expired but refresh token exists, try to refresh
    if (token && this.isTokenExpired(token) && refreshToken) {
      // Don't return false immediately, let the interceptor handle refresh
      return true;
    }

    // If access token is valid, user is logged in
    return !!token && !this.isTokenExpired(token);
  }

  // Check if token is about to expire (within 5 minutes)
  isTokenExpiringSoon(token: string | null): boolean {
    if (!token) return true;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000;
      const currentTime = Date.now();
      const fiveMinutes = 5 * 60 * 1000; // 5 minutes in milliseconds
      return expirationTime - currentTime < fiveMinutes;
    } catch {
      return true;
    }
  }

  // Attempt to refresh token if it's expired or expiring soon
  attemptTokenRefresh(): Observable<boolean> {
    const token = this.getAccessToken();
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      return of(false);
    }

    // If token is expired or expiring soon, refresh it
    if (
      !token ||
      this.isTokenExpired(token) ||
      this.isTokenExpiringSoon(token)
    ) {
      return this.refreshToken().pipe(
        map(() => true),
        catchError(() => of(false))
      );
    }

    return of(true);
  }

  refreshToken(): Observable<RefreshResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http
      .post<RefreshResponse>(`${this.baseUrl}${API_CONFIG.auth.refresh}`, {
        refreshToken,
      })
      .pipe(
        tap((res) => {
          const user = this.getUserInfo();
          if (user) {
            const updatedUser: StoredUser = {
              ...user,
              accessToken: res.accessToken,
            };
            localStorage.setItem('currentUser', JSON.stringify(updatedUser));
            this.userSubject.next(updatedUser);
          }
        }),
        catchError((error) => {
          console.error('Token refresh error:', error);
          this.logout(); // Logout if refresh fails
          return throwError(() => error);
        })
      );
  }

  isTokenExpired(token: string | null): boolean {
    if (!token) return true;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  // Role-based helper methods
  getUserRole(): string | null {
    const user = this.getUserInfo();
    return user?.role || null;
  }

  isAdmin(): boolean {
    return this.getUserRole() === 'ADMIN';
  }

  isStaff(): boolean {
    return this.getUserRole() === 'STAFF';
  }

  isClient(): boolean {
    return this.getUserRole() === 'CLIENT';
  }

  hasRole(role: string): boolean {
    return this.getUserRole() === role;
  }

  // Get current user's staff ID (only for staff users)
  getCurrentStaffId(): Observable<number | null> {
    if (!this.isStaff()) {
      return of(null);
    }

    return this.http
      .get<{ id: number }>(`${this.baseUrl}/api/staff/my-staff-id`)
      .pipe(
        map((response) => response.id),
        catchError(() => of(null))
      );
  }

  // Validate and refresh tokens on app startup
  validateAndRefreshTokens(): Observable<boolean> {
    const token = this.getAccessToken();
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      return of(false);
    }

    // If no access token but refresh token exists, try to refresh
    if (!token && refreshToken) {
      return this.refreshToken().pipe(
        map(() => true),
        catchError(() => of(false))
      );
    }

    // If access token exists but is expired, try to refresh
    if (token && this.isTokenExpired(token)) {
      return this.refreshToken().pipe(
        map(() => true),
        catchError(() => of(false))
      );
    }

    // If access token is valid, return true
    if (token && !this.isTokenExpired(token)) {
      return of(true);
    }

    return of(false);
  }

  // Clear all authentication data
  clearAuthData(): void {
    localStorage.removeItem('currentUser');
    this.userSubject.next(null);

    if (this.refreshTimer) {
      this.refreshTimer.unsubscribe();
      this.refreshTimer = null;
    }
  }

  private initializeTokenRefresh() {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      // Check if token needs immediate refresh
      const token = this.getAccessToken();
      if (token && this.isTokenExpired(token)) {
        // Token is expired, refresh immediately
        this.refreshToken().subscribe({
          next: () => {
            console.log('Token refreshed on initialization');
          },
          error: (error) => {
            console.error('Failed to refresh token on initialization:', error);
            // Don't logout immediately, let the interceptor handle it
          },
        });
      }

      // Set up periodic refresh (every 5 minutes)
      this.refreshTimer = timer(300000, 300000)
        .pipe(
          switchMap(() => {
            const currentToken = this.getAccessToken();
            if (currentToken && this.isTokenExpiringSoon(currentToken)) {
              return this.refreshToken();
            }
            return of(null);
          })
        )
        .subscribe({
          next: (result) => {
            if (result) {
              console.log('Token refreshed periodically');
            }
          },
          error: (error) => {
            console.error('Periodic token refresh failed:', error);
          },
        });
    }
  }

  private handlePageVisibilityChange(): void {
    document.addEventListener('visibilitychange', () => {
      if (!document.hidden) {
        // Page became visible, check if token needs refresh
        const token = this.getAccessToken();
        if (token && this.isTokenExpired(token)) {
          console.log(
            'Token expired while page was hidden, attempting refresh'
          );
          this.refreshToken().subscribe({
            next: () => {
              console.log('Token refreshed after page visibility change');
            },
            error: (error) => {
              console.error(
                'Failed to refresh token after page visibility change:',
                error
              );
            },
          });
        }
      }
    });
  }

  private handleBeforeUnload(): void {
    window.addEventListener('beforeunload', () => {
      // Save current timestamp to detect long absences
      localStorage.setItem('lastActivity', Date.now().toString());
    });
  }
}
