import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Subject, takeUntil, Observable, Subscription } from 'rxjs';
import { IssueService } from '../../../core/services/issue.service';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { SseService } from '../../../core/services/sse.service';
import { Issue, Comment, ActivityLog } from '../../../core/models/issue.model';
import { IssueStatus, IssuePriority } from '../../../core/models/enums';
import { User } from '../../../core/models/user.model';
import { ProjectMember } from '../../../core/models/project.model';

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
  projectMembers: ProjectMember[] = [];
  availableAssignees: User[] = [];  // Combined list for dropdown
  
  loading = true;
  loadingComments = false;
  loadingActivity = false;
  loadingMembers = false;
  error = '';
  
  currentUser$!: Observable<User | null>;  // Initialize in ngOnInit
  currentTab: 'comments' | 'activity' = 'comments';
  
  commentControl = new FormControl('', Validators.required);
  submittingComment = false;
  commentError = '';

  // Assignee update
  updatingAssignee = false;
  showAssigneeDropdown = false;

  // Comment editing
  editingCommentId: string | null = null;  // UUID
  editCommentControl = new FormControl('', Validators.required);
  updatingComment = false;

  showDeleteConfirm = false;
  deleting = false;
  deletingCommentId: string | null = null;  // UUID

  // Enums for template
  IssueStatus = IssueStatus;
  IssuePriority = IssuePriority;

  private destroy$ = new Subject<void>();
  private sseSubscription?: Subscription;

  constructor(
    private issueService: IssueService,
    private projectService: ProjectService,
    private authService: AuthService,
    private sseService: SseService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Initialize currentUser$ observable
    this.currentUser$ = this.authService.currentUser$;
    
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadIssue(id);  // Now a string (UUID)
      this.loadComments(id);
      this.loadActivityLog(id);
    }

    // Load current user if needed
    if (!this.authService.getCurrentUser()) {
      this.authService.loadCurrentUser();
    }

    // Subscribe to SSE for real-time updates
    if (id) {
      this.subscribeToIssueUpdates(id);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    
    // Clean up SSE subscription
    if (this.sseSubscription) {
      this.sseSubscription.unsubscribe();
    }
    if (this.issue) {
      this.sseService.closeConnection(`project-${this.issue.projectId}`);
    }
  }

  private subscribeToIssueUpdates(issueId: string): void {
    // We'll subscribe to the project's issues since we don't have issue-specific SSE
    // We'll filter for our specific issue
    this.issueService.getIssue(issueId).subscribe({
      next: (issue) => {
        const observable = this.sseService.subscribeToProjectIssues(issue.projectId);
        
        this.sseSubscription = observable.subscribe({
          next: (event) => {
            // Only process events for this specific issue
            if (event.data.id !== issueId) {
              return;
            }

            console.log('Issue update received:', event);
            
            switch (event.type) {
              case 'issue.updated':
              case 'issue.status.changed':
              case 'issue.priority.changed':
              case 'issue.assigned':
                this.handleIssueUpdated(event.data);
                break;
              
              case 'issue.deleted':
                this.handleIssueDeleted();
                break;
            }
          },
          error: (error) => {
            console.error('SSE connection error:', error);
          }
        });
      }
    });
  }

  private handleIssueUpdated(updatedIssue: Issue): void {
    if (this.issue) {
      const oldCommentCount = this.issue.commentCount;
      
      // Update issue data
      this.issue = { ...this.issue, ...updatedIssue };
      console.log('Issue updated:', updatedIssue.title);
      
      // Reload comments if comment count changed
      if (updatedIssue.commentCount !== oldCommentCount) {
        console.log('Comment count changed, reloading comments...');
        this.loadComments(this.issue.id);
      }
      
      // Always reload activity log to show the update
      this.loadActivityLog(this.issue.id);
    }
  }

  private handleIssueDeleted(): void {
    console.log('Issue was deleted, redirecting...');
    this.router.navigate(['/issues'], {
      queryParams: this.issue ? { projectId: this.issue.projectId } : {}
    });
  }

  loadIssue(id: string): void {  // UUID
    this.loading = true;
    this.error = '';

    this.issueService.getIssue(id).subscribe({
      next: (issue) => {
        this.issue = issue;
        this.loading = false;
        // Load project members for permission checks
        this.loadProjectMembers(issue.projectId);
      },
      error: (error) => {
        this.error = 'Failed to load issue';
        this.loading = false;
        console.error('Error loading issue:', error);
      }
    });
  }

  loadComments(issueId: string): void {  // UUID
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

  loadActivityLog(issueId: string): void {  // UUID
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
    
    // Issue reporter can edit
    if (this.issue.reporter?.id === currentUser.id) return true;
    
    // Project owner can edit
    if (this.issue.projectOwnerId === currentUser.id) return true;
    
    // Any project member can edit
    if (this.projectMembers.some(m => m.user.id === currentUser.id)) return true;
    
    return false;
  }

  canDeleteIssue(): boolean {
    // Only reporter and project owner can delete (not regular members)
    if (!this.issue) return false;
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return false;
    
    return (this.issue.reporter?.id === currentUser.id) || 
           (this.issue.projectOwnerId === currentUser.id);
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

  loadProjectMembers(projectId: string): void {  // UUID
    this.loadingMembers = true;
    
    // Load both project (for owner) and members
    this.projectService.getProject(projectId).subscribe({
      next: (project) => {
        this.projectService.getMembers(projectId).subscribe({
          next: (members: ProjectMember[]) => {
            this.projectMembers = members;
            
            // Build combined list of available assignees
            const assigneeMap = new Map<string, User>();  // UUID keys
            
            // Add project owner
            assigneeMap.set(project.owner.id, project.owner);
            
            // Add issue reporter
            if (this.issue?.reporter) {
              assigneeMap.set(this.issue.reporter.id, this.issue.reporter);
            }
            
            // Add all project members
            members.forEach(member => {
              assigneeMap.set(member.user.id, member.user);
            });
            
            // Convert map to array
            this.availableAssignees = Array.from(assigneeMap.values());
            this.loadingMembers = false;
          },
          error: (error: any) => {
            console.error('Error loading project members:', error);
            this.loadingMembers = false;
          }
        });
      },
      error: (error: any) => {
        console.error('Error loading project:', error);
        this.loadingMembers = false;
      }
    });
  }

  canUpdateAssignee(): boolean {
    if (!this.issue) return false;
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return false;
    
    // Project owner or members can update assignee
    return this.issue.projectOwnerId === currentUser.id ||
           this.projectMembers.some(m => m.user.id === currentUser.id);
  }

  toggleAssigneeDropdown(): void {
    // Don't need to load members again if already loaded
    this.showAssigneeDropdown = !this.showAssigneeDropdown;
  }

  updateAssignee(assigneeId: string | null): void {  // UUID
    if (!this.issue) return;

    this.updatingAssignee = true;
    this.issueService.updateIssue(this.issue.id, { assigneeId: assigneeId || undefined }).subscribe({
      next: (updatedIssue) => {
        this.issue = updatedIssue;
        this.updatingAssignee = false;
        this.showAssigneeDropdown = false;
        // Reload activity to show assignee change
        this.loadActivityLog(this.issue!.id);
      },
      error: (error) => {
        console.error('Error updating assignee:', error);
        this.updatingAssignee = false;
      }
    });
  }

  canEditComment(comment: Comment): boolean {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return false;
    return comment.user.id === currentUser.id;
  }

  startEditComment(comment: Comment): void {
    this.editingCommentId = comment.id;
    this.editCommentControl.setValue(comment.content);
  }

  cancelEditComment(): void {
    this.editingCommentId = null;
    this.editCommentControl.reset();
  }

  saveEditComment(comment: Comment): void {
    if (this.editCommentControl.invalid || !this.issue) return;

    this.updatingComment = true;
    this.issueService.updateComment(
      this.issue.id,
      comment.id,
      this.editCommentControl.value!
    ).subscribe({
      next: (updatedComment) => {
        // Update the comment in the local array
        const index = this.comments.findIndex(c => c.id === comment.id);
        if (index !== -1) {
          this.comments[index] = updatedComment;
        }
        this.editingCommentId = null;
        this.editCommentControl.reset();
        this.updatingComment = false;
        // Reload activity to show edit event
        this.loadActivityLog(this.issue!.id);
      },
      error: (error) => {
        console.error('Error updating comment:', error);
        alert('Failed to update comment: ' + (error.error?.message || 'Unknown error'));
        this.updatingComment = false;
      }
    });
  }

  confirmDeleteComment(commentId: string): void {  // UUID
    this.deletingCommentId = commentId;
  }

  cancelDeleteComment(): void {
    this.deletingCommentId = null;
  }

  deleteComment(commentId: string): void {  // UUID
    if (!this.issue) return;

    this.issueService.deleteComment(this.issue.id, commentId).subscribe({
      next: () => {
        // Remove the comment from local array
        this.comments = this.comments.filter(c => c.id !== commentId);
        this.deletingCommentId = null;
        // Reload activity to show delete event
        this.loadActivityLog(this.issue!.id);
      },
      error: (error) => {
        console.error('Error deleting comment:', error);
        alert('Failed to delete comment: ' + (error.error?.message || 'Unknown error'));
        this.deletingCommentId = null;
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
