import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { IssueService } from '../../../core/services/issue.service';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { Issue, CreateIssueRequest, UpdateIssueRequest } from '../../../core/models/issue.model';
import { IssueStatus, IssuePriority } from '../../../core/models/enums';
import { Project, ProjectMember } from '../../../core/models/project.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-issue-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './issue-form.component.html',
  styleUrls: ['./issue-form.component.scss']
})
export class IssueFormComponent implements OnInit {
  issueForm: FormGroup;
  loading = false;
  error = '';
  isEditMode = false;
  issueId?: string;  // UUID
  currentIssue?: Issue;  // Store current issue for getting reporter
  currentUser$!: Observable<User | null>;  // Initialize in ngOnInit

  projects: Project[] = [];
  members: ProjectMember[] = [];
  availableAssignees: User[] = [];  // Combined list for dropdown
  loadingProjects = true;
  loadingMembers = false;

  // Enums for template
  statusOptions = Object.values(IssueStatus);
  priorityOptions = Object.values(IssuePriority);

  constructor(
    private fb: FormBuilder,
    private issueService: IssueService,
    private projectService: ProjectService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.issueForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      projectId: ['', Validators.required],
      status: [IssueStatus.OPEN, Validators.required],
      priority: [IssuePriority.MEDIUM, Validators.required],
      assigneeId: ['']
    });
  }

  ngOnInit(): void {
    // Initialize currentUser$ observable
    this.currentUser$ = this.authService.currentUser$;
    
    // Load projects
    this.loadProjects();

    // Check if edit mode
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.isEditMode = true;
      this.issueId = id;  // Now a string (UUID)
      if (this.issueId) {  // Type guard for strict null check
        this.loadIssue(this.issueId);
      }
    } else {
      // Check for projectId query param
      this.route.queryParams.subscribe(params => {
        if (params['projectId']) {
          this.issueForm.patchValue({ projectId: params['projectId'] });  // Already a string
          this.loadMembers(params['projectId']);
        }
      });
    }

    // Watch project changes to load members
    this.issueForm.get('projectId')?.valueChanges.subscribe(projectId => {
      if (projectId) {
        this.loadMembers(projectId);  // Already a string (UUID)
      } else {
        this.members = [];
      }
    });

    // Load current user if needed
    if (!this.authService.getCurrentUser()) {
      this.authService.loadCurrentUser();
    }
  }

  loadProjects(): void {
    this.loadingProjects = true;
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.loadingProjects = false;
      },
      error: (error) => {
        this.error = 'Failed to load projects';
        this.loadingProjects = false;
        console.error('Error loading projects:', error);
      }
    });
  }

  loadMembers(projectId: string): void {  // UUID
    this.loadingMembers = true;
    
    // Load both project (for owner) and members
    this.projectService.getProject(projectId).subscribe({
      next: (project) => {
        this.projectService.getMembers(projectId).subscribe({
          next: (members) => {
            this.members = members;
            
            // Build combined list of available assignees
            const assigneeMap = new Map<string, User>();  // UUID keys
            
            // Add project owner
            assigneeMap.set(project.owner.id, project.owner);
            
            // Add issue reporter (if in edit mode)
            if (this.currentIssue?.reporter) {
              assigneeMap.set(this.currentIssue.reporter.id, this.currentIssue.reporter);
            }
            
            // Add all project members
            members.forEach(member => {
              assigneeMap.set(member.user.id, member.user);
            });
            
            // Convert map to array
            this.availableAssignees = Array.from(assigneeMap.values());
            this.loadingMembers = false;
          },
          error: (error) => {
            console.error('Error loading members:', error);
            this.members = [];
            this.availableAssignees = [];
            this.loadingMembers = false;
          }
        });
      },
      error: (error) => {
        console.error('Error loading project:', error);
        this.loadingMembers = false;
      }
    });
  }

  loadIssue(id: string): void {  // UUID
    this.loading = true;
    this.issueService.getIssue(id).subscribe({
      next: (issue) => {
        this.currentIssue = issue;  // Store for building assignee list
        this.issueForm.patchValue({
          title: issue.title,
          description: issue.description,
          projectId: issue.projectId,
          status: issue.status,
          priority: issue.priority,
          assigneeId: issue.assignee?.id || ''
        });
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load issue';
        this.loading = false;
        console.error('Error loading issue:', error);
      }
    });
  }

  onSubmit(): void {
    if (this.issueForm.invalid) {
      this.issueForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = '';

    const formValue = this.issueForm.value;
    const request: any = {
      title: formValue.title,
      description: formValue.description || undefined,
      status: formValue.status,
      priority: formValue.priority
    };
    
    // Handle projectId (required for create, not sent for update)
    if (!this.isEditMode) {
      request.projectId = formValue.projectId;
    }
    
    // Handle assigneeId: 
    // - For create: only send if a user is selected
    // - For update: always send (null means unassign, UUID means assign)
    if (this.isEditMode) {
      request.assigneeId = formValue.assigneeId || null;
    } else {
      if (formValue.assigneeId) {
        request.assigneeId = formValue.assigneeId;
      }
    }

    const operation = this.isEditMode && this.issueId
      ? this.issueService.updateIssue(this.issueId, request as UpdateIssueRequest)
      : this.issueService.createIssue(request as CreateIssueRequest);

    operation.subscribe({
      next: (issue) => {
        this.router.navigate(['/issues', issue.id]);
      },
      error: (error) => {
        this.error = error.error?.message || `Failed to ${this.isEditMode ? 'update' : 'create'} issue`;
        this.loading = false;
        console.error('Error saving issue:', error);
      }
    });
  }

  cancel(): void {
    if (this.isEditMode && this.issueId) {
      this.router.navigate(['/issues', this.issueId]);
    } else {
      this.router.navigate(['/issues']);
    }
  }

  getFieldError(fieldName: string): string {
    const field = this.issueForm.get(fieldName);
    if (field?.hasError('required') && field.touched) {
      return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} is required`;
    }
    if (field?.hasError('minlength') && field.touched) {
      return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must be at least 3 characters`;
    }
    return '';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
