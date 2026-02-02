import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { Project, ProjectMember } from '../../../core/models/project.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.scss']
})
export class ProjectDetailComponent implements OnInit {
  project?: Project;
  members: ProjectMember[] = [];
  loading = true;
  error = '';
  currentUser$!: Observable<User | null>;  // Initialize in ngOnInit
  
  showAddMemberForm = false;
  addMemberForm: FormGroup;
  addingMember = false;
  addMemberError = '';

  showDeleteConfirm = false;
  deleting = false;

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.addMemberForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    // Initialize currentUser$ observable
    this.currentUser$ = this.authService.currentUser$;
    
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadProject(id);  // Now a string (UUID)
    }

    // Load current user if needed
    if (!this.authService.getCurrentUser()) {
      this.authService.loadCurrentUser();
    }
  }

  loadProject(id: string): void {  // UUID
    this.loading = true;
    this.error = '';

    this.projectService.getProject(id).subscribe({
      next: (project) => {
        this.project = project;
        this.members = project.members || [];
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load project';
        this.loading = false;
        console.error('Error loading project:', error);
      }
    });
  }

  isOwner(): boolean {
    if (!this.project) return false;
    const currentUser = this.authService.getCurrentUser();
    return currentUser ? this.project.owner.id === currentUser.id : false;
  }

  editProject(): void {
    if (this.project) {
      this.router.navigate(['/projects', this.project.id, 'edit']);
    }
  }

  confirmDelete(): void {
    this.showDeleteConfirm = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
  }

  deleteProject(): void {
    if (!this.project) return;

    this.deleting = true;
    this.projectService.deleteProject(this.project.id).subscribe({
      next: () => {
        this.router.navigate(['/projects']);
      },
      error: (error) => {
        this.error = 'Failed to delete project';
        this.deleting = false;
        this.showDeleteConfirm = false;
        console.error('Error deleting project:', error);
      }
    });
  }

  toggleAddMemberForm(): void {
    this.showAddMemberForm = !this.showAddMemberForm;
    this.addMemberError = '';
    this.addMemberForm.reset();
  }

  addMember(): void {
    if (this.addMemberForm.invalid || !this.project) return;

    this.addingMember = true;
    this.addMemberError = '';

    this.projectService.addMember(this.project.id, this.addMemberForm.value).subscribe({
      next: (member) => {
        this.members.push(member);
        this.showAddMemberForm = false;
        this.addMemberForm.reset();
        this.addingMember = false;
        // Reload to get updated member count
        this.loadProject(this.project!.id);
      },
      error: (error) => {
        this.addMemberError = error.error?.message || 'Failed to add member';
        this.addingMember = false;
        console.error('Error adding member:', error);
      }
    });
  }

  removeMember(member: ProjectMember): void {
    if (!this.project) return;
    if (!confirm(`Remove ${member.user.fullName} from this project?`)) return;

    this.projectService.removeMember(this.project.id, member.user.id).subscribe({
      next: () => {
        this.members = this.members.filter(m => m.id !== member.id);
        // Reload to get updated member count
        this.loadProject(this.project!.id);
      },
      error: (error) => {
        this.error = 'Failed to remove member';
        console.error('Error removing member:', error);
      }
    });
  }

  viewIssues(): void {
    if (this.project) {
      this.router.navigate(['/issues'], { queryParams: { projectId: this.project.id } });
    }
  }

  backToProjects(): void {
    this.router.navigate(['/projects']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
