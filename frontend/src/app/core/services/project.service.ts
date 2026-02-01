import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Project,
  CreateProjectRequest,
  UpdateProjectRequest,
  ProjectMember,
  AddMemberRequest
} from '../models/project.model';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private readonly API_URL = `${environment.apiUrl}/projects`;

  constructor(private http: HttpClient) {}

  /**
   * Get all projects for current user
   */
  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.API_URL);
  }

  /**
   * Get project by ID
   */
  getProject(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.API_URL}/${id}`);
  }

  /**
   * Create new project
   */
  createProject(request: CreateProjectRequest): Observable<Project> {
    return this.http.post<Project>(this.API_URL, request);
  }

  /**
   * Update project
   */
  updateProject(id: number, request: UpdateProjectRequest): Observable<Project> {
    return this.http.put<Project>(`${this.API_URL}/${id}`, request);
  }

  /**
   * Delete project
   */
  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Get project members
   */
  getMembers(projectId: number): Observable<ProjectMember[]> {
    return this.http.get<ProjectMember[]>(`${this.API_URL}/${projectId}/members`);
  }

  /**
   * Add member to project
   */
  addMember(projectId: number, request: AddMemberRequest): Observable<ProjectMember> {
    return this.http.post<ProjectMember>(`${this.API_URL}/${projectId}/members`, request);
  }

  /**
   * Remove member from project
   */
  removeMember(projectId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${projectId}/members/${userId}`);
  }
}
