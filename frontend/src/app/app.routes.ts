import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';

export const routes: Routes = [
  // Default route
  {
    path: '',
    redirectTo: '/projects',
    pathMatch: 'full'
  },
  
  // Auth routes (public)
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  
  // Protected routes (require authentication)
  {
    path: 'projects',
    canActivate: [authGuard],
    loadChildren: () => import('./features/projects/projects.routes').then(m => m.projectsRoutes)
  },
  {
    path: 'issues',
    canActivate: [authGuard],
    loadChildren: () => import('./features/issues/issues.routes').then(m => m.issuesRoutes)
  },
  
  // Wildcard route
  {
    path: '**',
    redirectTo: '/projects'
  }
];
