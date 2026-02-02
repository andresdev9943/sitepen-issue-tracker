import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, takeUntil, Observable, Subscription } from 'rxjs';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { IssueService } from '../../../core/services/issue.service';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { SseService } from '../../../core/services/sse.service';
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

  // Sorting
  sortBy = 'createdAt';
  sortDir = 'desc';
  sortOptions = [
    { label: 'Newest First', value: 'createdAt:desc' },
    { label: 'Oldest First', value: 'createdAt:asc' },
    { label: 'Priority: High to Low', value: 'priority:desc' },
    { label: 'Priority: Low to High', value: 'priority:asc' },
    { label: 'Title: A-Z', value: 'title:asc' },
    { label: 'Title: Z-A', value: 'title:desc' },
    { label: 'Status: Open First', value: 'status:asc' },
    { label: 'Status: Closed First', value: 'status:desc' }
  ];

  // Enums for template
  IssueStatus = IssueStatus;
  IssuePriority = IssuePriority;
  statusOptions = Object.values(IssueStatus);
  priorityOptions = Object.values(IssuePriority);

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

    // Subscribe to SSE for real-time updates
    this.subscribeToIssueUpdates();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    
    // Clean up SSE subscriptions
    if (this.sseSubscription) {
      this.sseSubscription.unsubscribe();
    }
    if (this.selectedProjectId) {
      this.sseService.closeConnection(`project-${this.selectedProjectId}`);
    } else {
      this.sseService.closeConnection('all-issues');
    }
  }

  private subscribeToIssueUpdates(): void {
    // Subscribe to project-specific or all issues based on filter
    const observable = this.selectedProjectId
      ? this.sseService.subscribeToProjectIssues(this.selectedProjectId)
      : this.sseService.subscribeToAllIssues();

    this.sseSubscription = observable.subscribe({
      next: (event) => {
        console.log('Issue event received:', event);
        
        switch (event.type) {
          case 'issue.created':
            this.handleIssueCreated(event.data);
            break;
          
          case 'issue.updated':
          case 'issue.status.changed':
          case 'issue.priority.changed':
          case 'issue.assigned':
            this.handleIssueUpdated(event.data);
            break;
          
          case 'issue.deleted':
            this.handleIssueDeleted(event.data);
            break;
        }
      },
      error: (error) => {
        console.error('SSE connection error:', error);
      }
    });
  }

  private handleIssueCreated(issue: Issue): void {
    // Check if issue matches current filters
    if (!this.matchesFilters(issue)) {
      return;
    }

    // Add to list if not already present
    const exists = this.issues.some(i => i.id === issue.id);
    if (!exists) {
      this.issues.push(issue);
      this.totalElements++;
      // Re-sort the main issues array for list view
      this.issues = this.sortIssues(this.issues);
      // Organize for board view
      this.organizeIssuesForBoard();
      console.log('Issue added to list:', issue.title);
    }
  }

  private handleIssueUpdated(issue: Issue): void {
    // Update issue in list
    const index = this.issues.findIndex(i => i.id === issue.id);
    
    if (index !== -1) {
      // Check if issue still matches filters
      if (this.matchesFilters(issue)) {
        this.issues[index] = issue;
        // Re-sort the main issues array for list view (title, priority, etc might have changed)
        this.issues = this.sortIssues([...this.issues]);
        // Organize for board view
        this.organizeIssuesForBoard();
        console.log('Issue updated in list:', issue.title);
      } else {
        // Issue no longer matches filters, remove it
        this.handleIssueDeleted(issue);
      }
    } else if (this.matchesFilters(issue)) {
      // Issue wasn't in list but now matches filters
      this.handleIssueCreated(issue);
    }
  }

  private handleIssueDeleted(issue: Issue): void {
    // Remove issue from list
    const originalLength = this.issues.length;
    this.issues = this.issues.filter(i => i.id !== issue.id);
    
    if (this.issues.length < originalLength) {
      this.totalElements = Math.max(0, this.totalElements - 1);
      this.organizeIssuesForBoard();
      console.log('Issue removed from list:', issue.title);
    }
  }

  private matchesFilters(issue: Issue): boolean {
    // Check project filter
    if (this.selectedProjectId && issue.projectId !== this.selectedProjectId) {
      return false;
    }

    // Check status filter
    if (this.selectedStatus && issue.status !== this.selectedStatus) {
      return false;
    }

    // Check priority filter
    if (this.selectedPriority && issue.priority !== this.selectedPriority) {
      return false;
    }

    // Check search text (simplified - backend does full text search)
    const searchText = this.searchControl.value?.toLowerCase();
    if (searchText && !issue.title.toLowerCase().includes(searchText)) {
      return false;
    }

    return true;
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

    // Construct sort parameter in the format "field,direction"
    const sortParam = `${this.sortBy},${this.sortDir}`;

    this.issueService
      .getIssues(
        this.selectedProjectId,
        this.selectedStatus,
        this.selectedPriority,
        undefined, // assigneeId
        this.searchControl.value || undefined,
        this.currentPage,
        this.pageSize,
        sortParam
      )
      .subscribe({
        next: (response: PageResponse<Issue>) => {
          this.issues = response.content;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.currentPage = response.number;
          this.loading = false;
          
          // Apply client-side sorting to ensure list view is sorted correctly
          this.issues = this.sortIssues(this.issues);
          
          // Organize issues for board view (applies sorting to each column)
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
    // Filter by status
    this.openIssues = this.issues.filter(issue => issue.status === IssueStatus.OPEN);
    this.inProgressIssues = this.issues.filter(issue => issue.status === IssueStatus.IN_PROGRESS);
    this.closedIssues = this.issues.filter(issue => issue.status === IssueStatus.CLOSED);
    
    // Sort each column independently based on current sort criteria
    this.openIssues = this.sortIssues(this.openIssues);
    this.inProgressIssues = this.sortIssues(this.inProgressIssues);
    this.closedIssues = this.sortIssues(this.closedIssues);
  }

  private sortIssues(issues: Issue[]): Issue[] {
    return [...issues].sort((a, b) => {
      let comparison = 0;
      
      switch (this.sortBy) {
        case 'createdAt':
          comparison = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
          break;
        case 'priority':
          // Priority order: CRITICAL > HIGH > MEDIUM > LOW
          const priorityOrder = { 'CRITICAL': 4, 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 };
          comparison = priorityOrder[a.priority] - priorityOrder[b.priority];
          break;
        case 'title':
          comparison = a.title.localeCompare(b.title, undefined, { sensitivity: 'base' });
          break;
        case 'status':
          // Status order: OPEN > IN_PROGRESS > CLOSED
          const statusOrder = { 'OPEN': 1, 'IN_PROGRESS': 2, 'CLOSED': 3 };
          comparison = statusOrder[a.status] - statusOrder[b.status];
          break;
        default:
          comparison = 0;
      }
      
      // Apply sort direction
      return this.sortDir === 'desc' ? -comparison : comparison;
    });
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

  onSortChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const [field, direction] = select.value.split(':');
    this.sortBy = field;
    this.sortDir = direction;
    this.currentPage = 0;
    this.loadIssues();
  }

  getCurrentSortValue(): string {
    return `${this.sortBy}:${this.sortDir}`;
  }

  clearFilters(): void {
    this.selectedProjectId = undefined;
    this.selectedStatus = undefined;
    this.selectedPriority = undefined;
    this.searchControl.setValue('');
    this.sortBy = 'createdAt';
    this.sortDir = 'desc';
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
