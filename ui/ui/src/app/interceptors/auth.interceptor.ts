import { inject } from '@angular/core';
import {
  HttpRequest,
  HttpHandlerFn,
  HttpEvent,
  HttpInterceptorFn,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

const PUBLIC_ENDPOINTS = [
  '/api/auth/login',
  '/api/auth/refresh',
  '/api/auth/register',
  '/api/leads/public',
  '/api/services/hierarchy',
  '/api/services/categories',
  '/api/services/subcategories',
  '/api/services/items',
];

export const AuthInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Skip adding token for public endpoints
  if (PUBLIC_ENDPOINTS.some((endpoint) => request.url.includes(endpoint))) {
    return next(request);
  }

  const token = authService.getAccessToken();

  // If no token and no refresh token, redirect to login only for protected routes
  if (!token && !authService.getRefreshToken()) {
    // Only redirect if we're not already on a public route
    if (
      !router.url.includes('/login') &&
      !router.url.includes('/landing') &&
      !router.url.includes('/lead-form')
    ) {
      authService.logout();
      router.navigate(['/login']);
    }
    return throwError(() => new Error('No authentication tokens available'));
  }

  if (token) {
    request = addToken(request, token);
  }

  return next(request).pipe(
    catchError((error) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        authService.getRefreshToken()
      ) {
        return handle401Error(request, next, authService, router);
      }

      // If it's a 401 and no refresh token, logout only for protected routes
      if (error instanceof HttpErrorResponse && error.status === 401) {
        if (
          !router.url.includes('/login') &&
          !router.url.includes('/landing') &&
          !router.url.includes('/lead-form')
        ) {
          authService.logout();
          router.navigate(['/login']);
        }
      }

      return throwError(() => error);
    })
  );
};

const addToken = (
  request: HttpRequest<unknown>,
  token: string
): HttpRequest<unknown> => {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });
};

const handle401Error = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authService: AuthService,
  router: Router
): Observable<HttpEvent<unknown>> => {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshToken().pipe(
      switchMap((response) => {
        isRefreshing = false;
        refreshTokenSubject.next(response.accessToken);
        return next(addToken(request, response.accessToken));
      }),
      catchError((error) => {
        isRefreshing = false;
        refreshTokenSubject.next(null);

        // Clear any stored tokens
        authService.logout();

        // Only redirect to login if we're not already there
        if (!router.url.includes('/login')) {
          router.navigate(['/login']);
        }

        return throwError(() => error);
      })
    );
  }

  return refreshTokenSubject.pipe(
    filter((token): token is string => token !== null),
    take(1),
    switchMap((token) => next(addToken(request, token))),
    catchError((error) => {
      // If refresh token is null, it means refresh failed
      if (refreshTokenSubject.value === null) {
        authService.logout();
        if (!router.url.includes('/login')) {
          router.navigate(['/login']);
        }
      }
      return throwError(() => error);
    })
  );
};
