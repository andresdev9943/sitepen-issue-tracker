import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ProjectService } from '../../../core/services/project.service';
import { SseService } from '../../../core/services/sse.service';
import { Project } from '../../../core/models/project.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
})
export class ProjectListComponent implements OnInit, OnDestroy {
  projects: Project[] = [];
  loading = true;
  error = '';
  currentUser$!: Observable<User | null>;  // Initialize in ngOnInit
  private sseSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private projectService: ProjectService,
    private sseService: SseService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Initialize currentUser$ observable
    this.currentUser$ = this.authService.currentUser$;
    
    this.loadProjects();
    
    // Load current user if not already loaded
    if (!this.authService.getCurrentUser()) {
      this.authService.loadCurrentUser();
    }

    // Subscribe to user-specific SSE events
    this.subscribeToUserEvents();
  }

  ngOnDestroy(): void {
    // Clean up SSE subscription
    if (this.sseSubscription) {
      this.sseSubscription.unsubscribe();
    }
    this.sseService.closeConnection('user-events');
  }

  private subscribeToUserEvents(): void {
    this.sseSubscription = this.sseService.subscribeToUserEvents().subscribe({
      next: (event) => {
        console.log('User event received:', event);
        
        switch (event.type) {
          case 'project.member.added':
            // A new project was added to user's list
            this.handleProjectAdded(event.data);
            break;
          
          case 'project.member.removed':
            // User was removed from a project
            this.handleProjectRemoved(event.data);
            break;
          
          case 'project.updated':
            // Project details were updated
            this.handleProjectUpdated(event.data);
            break;
          
          case 'project.deleted':
            // Project was deleted
            this.handleProjectDeleted(event.data);
            break;
        }
      },
      error: (error) => {
        console.error('SSE connection error:', error);
        // Optionally retry connection
      }
    });
  }

  private handleProjectAdded(project: Project): void {
    // Add project to list if not already present
    const exists = this.projects.some(p => p.id === project.id);
    if (!exists) {
      this.projects = [project, ...this.projects];
      console.log('Project added to list:', project.name);
    }
  }

  private handleProjectRemoved(project: Project): void {
    // Remove project from list
    this.projects = this.projects.filter(p => p.id !== project.id);
    console.log('Project removed from list:', project.name);
    
    // If currently viewing this project, redirect to project list
    const currentRoute = this.router.url;
    if (currentRoute.includes(`/projects/${project.id}`)) {
      this.router.navigate(['/projects']);
    }
  }

  private handleProjectUpdated(project: Project): void {
    // Update project in list
    const index = this.projects.findIndex(p => p.id === project.id);
    if (index !== -1) {
      this.projects[index] = project;
      this.projects = [...this.projects]; // Trigger change detection
      console.log('Project updated in list:', project.name);
    }
  }

  private handleProjectDeleted(project: Project): void {
    // Remove project from list (same as removal)
    this.handleProjectRemoved(project);
  }

  loadProjects(): void {
    this.loading = true;
    this.error = '';
    
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load projects';
        this.loading = false;
        console.error('Error loading projects:', error);
      }
    });
  }

  viewProject(id: string): void {  // UUID
    this.router.navigate(['/projects', id]);
  }

  createProject(): void {
    this.router.navigate(['/projects/new']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  isOwner(project: Project): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser ? project.owner.id === currentUser.id : false;
  }
}
