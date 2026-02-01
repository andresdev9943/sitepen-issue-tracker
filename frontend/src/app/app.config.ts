import { ApplicationConfig, provideZoneChangeDetection, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthService } from './core/services/auth.service';
import { firstValueFrom, timeout } from 'rxjs';
import { filter, take } from 'rxjs/operators';

// Initialize auth before app starts
export function initializeAuth(authService: AuthService) {
  return () => {
    // If token exists, load the user
    if (authService.isAuthenticated()) {
      // Explicitly trigger user loading
      authService.loadCurrentUser();
      
      // Wait for user to be loaded
      return firstValueFrom(
        authService.currentUser$.pipe(
          filter(user => user !== null),
          take(1),
          timeout(3000) // Timeout after 3 seconds
        )
      ).catch((error) => {
        // Log error but continue app initialization
        console.warn('Failed to initialize user on app start:', error);
        // If timeout or error, continue anyway
        return Promise.resolve();
      });
    }
    return Promise.resolve();
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAuth,
      deps: [AuthService],
      multi: true
    }
  ],
};
