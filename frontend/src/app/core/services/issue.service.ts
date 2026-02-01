import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Issue,
  CreateIssueRequest,
  UpdateIssueRequest,
  Comment,
  CreateCommentRequest,
  ActivityLog,
  PageResponse
} from '../models/issue.model';
import { IssueStatus, IssuePriority } from '../models/enums';

@Injectable({
  providedIn: 'root'
})
export class IssueService {
  private readonly API_URL = `${environment.apiUrl}/issues`;

  constructor(private http: HttpClient) {}

  /**
   * Get paginated and filtered issues
   */
  getIssues(
    projectId?: number,
    status?: IssueStatus,
    priority?: IssuePriority,
    assigneeId?: number,
    search?: string,
    page: number = 0,
    size: number = 20,
    sort: string = 'createdAt,desc'
  ): Observable<PageResponse<Issue>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (projectId !== undefined) {
      params = params.set('projectId', projectId.toString());
    }
    if (status) {
      params = params.set('status', status);
    }
    if (priority) {
      params = params.set('priority', priority);
    }
    if (assigneeId !== undefined) {
      params = params.set('assigneeId', assigneeId.toString());
    }
    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<PageResponse<Issue>>(this.API_URL, { params });
  }

  /**
   * Get single issue by ID
   */
  getIssue(id: number): Observable<Issue> {
    return this.http.get<Issue>(`${this.API_URL}/${id}`);
  }

  /**
   * Create new issue
   */
  createIssue(request: CreateIssueRequest): Observable<Issue> {
    return this.http.post<Issue>(this.API_URL, request);
  }

  /**
   * Update existing issue
   */
  updateIssue(id: number, request: UpdateIssueRequest): Observable<Issue> {
    return this.http.put<Issue>(`${this.API_URL}/${id}`, request);
  }

  /**
   * Delete issue
   */
  deleteIssue(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Get comments for an issue
   */
  getComments(issueId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.API_URL}/${issueId}/comments`);
  }

  /**
   * Add comment to issue
   */
  addComment(issueId: number, request: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${this.API_URL}/${issueId}/comments`, request);
  }

  /**
   * Get activity log for an issue
   */
  getActivityLog(issueId: number): Observable<ActivityLog[]> {
    return this.http.get<ActivityLog[]>(`${this.API_URL}/${issueId}/activity`);
  }

  /**
   * Get issues by project (non-paginated)
   */
  getIssuesByProject(projectId: number): Observable<Issue[]> {
    return this.http.get<Issue[]>(`${this.API_URL}/project/${projectId}`);
  }
}
