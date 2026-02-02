import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, takeUntil, Observable } from 'rxjs';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { IssueService } from '../../../core/services/issue.service';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { Issue, PageResponse } from '../../../core/models/issue.model';
import { IssueStatus, IssuePriority } from '../../../core/models/enums';
import { Project } from '../../../core/models/project.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-issue-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, DragDropModule],
  templateUrl: './issue-list.component.html',
  styleUrls: ['./issue-list.component.scss']
})
export class IssueListComponent implements OnInit, OnDestroy {
  issues: Issue[] = [];
  projects: Project[] = [];
  loading = true;
  error = '';
  currentUser$!: Observable<User | null>;  // Initialize in ngOnInit

  // View mode
  viewMode: 'list' | 'board' = 'board';  // Default to board view

  // Board view data
  openIssues: Issue[] = [];
  inProgressIssues: Issue[] = [];
  closedIssues: Issue[] = [];

  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalPages = 0;
  totalElements = 0;

  // Filters
  selectedProjectId?: string;  // UUID
  selectedStatus?: IssueStatus;
  selectedPriority?: IssuePriority;
  searchControl = new FormControl('');

  // Enums for template
  IssueStatus = IssueStatus;
  IssuePriority = IssuePriority;
  statusOptions = Object.values(IssueStatus);
  priorityOptions = Object.values(IssuePriority);

  private destroy$ = new Subject<void>();

  constructor(
    private issueService: IssueService,
    private projectService: ProjectService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Initialize currentUser$ observable
    this.currentUser$ = this.authService.currentUser$;
    
    // Load projects for filter dropdown
    this.loadProjects();

    // Check for projectId query param
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['projectId']) {
        this.selectedProjectId = params['projectId'];  // Already a string (UUID)
      }
      this.loadIssues();
    });

    // Load current user if needed
    if (!this.authService.getCurrentUser()) {
      this.authService.loadCurrentUser();
    }

    // Setup search debounce
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.currentPage = 0;
        this.loadIssues();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadProjects(): void {
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
      },
      error: (error) => {
        console.error('Error loading projects:', error);
      }
    });
  }

  loadIssues(): void {
    this.loading = true;
    this.error = '';

    this.issueService
      .getIssues(
        this.selectedProjectId,
        this.selectedStatus,
        this.selectedPriority,
        undefined, // assigneeId
        this.searchControl.value || undefined,
        this.currentPage,
        this.pageSize
      )
      .subscribe({
        next: (response: PageResponse<Issue>) => {
          this.issues = response.content;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.currentPage = response.number;
          this.loading = false;
          
          // Organize issues for board view
          this.organizeIssuesForBoard();
        },
        error: (error) => {
          this.error = 'Failed to load issues';
          this.loading = false;
          console.error('Error loading issues:', error);
        }
      });
  }

  organizeIssuesForBoard(): void {
    this.openIssues = this.issues.filter(issue => issue.status === IssueStatus.OPEN);
    this.inProgressIssues = this.issues.filter(issue => issue.status === IssueStatus.IN_PROGRESS);
    this.closedIssues = this.issues.filter(issue => issue.status === IssueStatus.CLOSED);
  }

  toggleView(mode: 'list' | 'board'): void {
    this.viewMode = mode;
  }

  drop(event: CdkDragDrop<Issue[]>, newStatus: IssueStatus): void {
    if (event.previousContainer === event.container) {
      // Same column - just reorder
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      // Different column - move and update status
      const issue = event.previousContainer.data[event.previousIndex];
      
      // Transfer the item
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );

      // Update issue status via API
      this.updateIssueStatus(issue, newStatus);
    }
  }

  updateIssueStatus(issue: Issue, newStatus: IssueStatus): void {
    this.issueService.updateIssue(issue.id, { status: newStatus }).subscribe({
      next: (updatedIssue) => {
        issue.status = updatedIssue.status;
        console.log(`Issue #${issue.id} status updated to ${newStatus}`);
      },
      error: (error) => {
        console.error('Error updating issue status:', error);
        // Reload issues on error to revert the UI change
        this.loadIssues();
      }
    });
  }

  getStatusColumnId(status: IssueStatus): string {
    return `status-${status.toLowerCase().replace('_', '-')}`;
  }

  onProjectFilterChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedProjectId = select.value || undefined;  // Already a string (UUID)
    this.currentPage = 0;
    this.loadIssues();
  }

  onStatusFilterChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedStatus = select.value ? (select.value as IssueStatus) : undefined;
    this.currentPage = 0;
    this.loadIssues();
  }

  onPriorityFilterChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedPriority = select.value ? (select.value as IssuePriority) : undefined;
    this.currentPage = 0;
    this.loadIssues();
  }

  clearFilters(): void {
    this.selectedProjectId = undefined;
    this.selectedStatus = undefined;
    this.selectedPriority = undefined;
    this.searchControl.setValue('');
    this.currentPage = 0;
    this.loadIssues();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadIssues();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadIssues();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadIssues();
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

  createIssue(): void {
    if (this.selectedProjectId) {
      this.router.navigate(['/issues/new'], {
        queryParams: { projectId: this.selectedProjectId }
      });
    } else {
      this.router.navigate(['/issues/new']);
    }
  }

  viewIssue(issue: Issue): void {
    this.router.navigate(['/issues', issue.id]);
  }

  backToProjects(): void {
    this.router.navigate(['/projects']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
