import { Routes } from '@angular/router';

export const issuesRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./issue-list/issue-list.component').then(m => m.IssueListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./issue-form/issue-form.component').then(m => m.IssueFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./issue-detail/issue-detail.component').then(m => m.IssueDetailComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./issue-form/issue-form.component').then(m => m.IssueFormComponent)
  }
];
