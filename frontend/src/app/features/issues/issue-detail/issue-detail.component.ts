import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Subject, takeUntil, Observable } from 'rxjs';
import { IssueService } from '../../../core/services/issue.service';
import { AuthService } from '../../../core/services/auth.service';
import { Issue, Comment, ActivityLog } from '../../../core/models/issue.model';
import { IssueStatus, IssuePriority } from '../../../core/models/enums';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-issue-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './issue-detail.component.html',
  styleUrls: ['./issue-detail.component.scss']
})
export class IssueDetailComponent implements OnInit, OnDestroy {
  issue?: Issue;
  comments: Comment[] = [];
  activityLog: ActivityLog[] = [];
  
  loading = true;
  loadingComments = false;
  loadingActivity = false;
  error = '';
  
  currentUser$!: Observable<User | null>;  // Initialize in ngOnInit
  currentTab: 'comments' | 'activity' = 'comments';
  
  commentControl = new FormControl('', Validators.required);
  submittingComment = false;
  commentError = '';

  showDeleteConfirm = false;
  deleting = false;

  // Enums for template
  IssueStatus = IssueStatus;
  IssuePriority = IssuePriority;

  private destroy$ = new Subject<void>();

  constructor(
    private issueService: IssueService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Initialize currentUser$ observable
    this.currentUser$ = this.authService.currentUser$;
    
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadIssue(+id);
      this.loadComments(+id);
      this.loadActivityLog(+id);
    }

    // Load current user if needed
    if (!this.authService.getCurrentUser()) {
      this.authService.loadCurrentUser();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadIssue(id: number): void {
    this.loading = true;
    this.error = '';

    this.issueService.getIssue(id).subscribe({
      next: (issue) => {
        this.issue = issue;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load issue';
        this.loading = false;
        console.error('Error loading issue:', error);
      }
    });
  }

  loadComments(issueId: number): void {
    this.loadingComments = true;
    this.issueService.getComments(issueId).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.loadingComments = false;
      },
      error: (error) => {
        console.error('Error loading comments:', error);
        this.loadingComments = false;
      }
    });
  }

  loadActivityLog(issueId: number): void {
    this.loadingActivity = true;
    this.issueService.getActivityLog(issueId).subscribe({
      next: (activity) => {
        this.activityLog = activity;
        this.loadingActivity = false;
      },
      error: (error) => {
        console.error('Error loading activity:', error);
        this.loadingActivity = false;
      }
    });
  }

  switchTab(tab: 'comments' | 'activity'): void {
    this.currentTab = tab;
  }

  submitComment(): void {
    if (this.commentControl.invalid || !this.issue) return;

    this.submittingComment = true;
    this.commentError = '';

    this.issueService.addComment(this.issue.id, {
      content: this.commentControl.value!
    }).subscribe({
      next: (comment) => {
        this.comments.push(comment);
        this.commentControl.reset();
        this.submittingComment = false;
        // Reload activity to show new comment event
        this.loadActivityLog(this.issue!.id);
      },
      error: (error) => {
        this.commentError = error.error?.message || 'Failed to add comment';
        this.submittingComment = false;
        console.error('Error adding comment:', error);
      }
    });
  }

  canEditIssue(): boolean {
    if (!this.issue) return false;
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return false;
    
    // Owner can edit, or project owner can edit
    return this.issue.reporter.id === currentUser.id || 
           this.issue.projectOwnerId === currentUser.id;
  }

  canDeleteIssue(): boolean {
    return this.canEditIssue();
  }

  editIssue(): void {
    if (this.issue) {
      this.router.navigate(['/issues', this.issue.id, 'edit']);
    }
  }

  confirmDelete(): void {
    this.showDeleteConfirm = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
  }

  deleteIssue(): void {
    if (!this.issue) return;

    this.deleting = true;
    this.issueService.deleteIssue(this.issue.id).subscribe({
      next: () => {
        this.router.navigate(['/issues'], {
          queryParams: { projectId: this.issue!.projectId }
        });
      },
      error: (error) => {
        this.error = 'Failed to delete issue';
        this.deleting = false;
        this.showDeleteConfirm = false;
        console.error('Error deleting issue:', error);
      }
    });
  }

  getPriorityClass(priority: IssuePriority): string {
    return `priority-${priority.toLowerCase()}`;
  }

  getStatusClass(status: IssueStatus): string {
    return `status-${status.toLowerCase().replace('_', '-')}`;
  }

  getStatusLabel(status: IssueStatus): string {
    return status.replace('_', ' ');
  }

  backToIssues(): void {
    if (this.issue) {
      this.router.navigate(['/issues'], {
        queryParams: { projectId: this.issue.projectId }
      });
    } else {
      this.router.navigate(['/issues']);
    }
  }

  viewProject(): void {
    if (this.issue) {
      this.router.navigate(['/projects', this.issue.projectId]);
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
