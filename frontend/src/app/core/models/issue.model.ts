import { User } from './user.model';
import { IssueStatus, IssuePriority } from './enums';

export interface Issue {
  id: number;
  projectId: number;
  projectName: string;
  title: string;
  description: string;
  status: IssueStatus;
  priority: IssuePriority;
  assignee: User | null;
  reporter: User;  // Changed from createdBy to reporter
  projectOwnerId: number;  // Added for permission checks
  createdAt: string;
  updatedAt: string;
  commentCount: number;
}

export interface CreateIssueRequest {
  projectId: number;
  title: string;
  description: string;
  priority?: IssuePriority;
  assigneeId?: number;
}

export interface UpdateIssueRequest {
  title?: string;
  description?: string;
  status?: IssueStatus;
  priority?: IssuePriority;
  assigneeId?: number;
}

export interface Comment {
  id: number;
  issueId: number;
  user: User;  // Backend sends 'user', not 'author'
  content: string;
  createdAt: string;
}

// Alias for template compatibility
export type CommentAuthor = User;

export interface CreateCommentRequest {
  content: string;
}

export interface ActivityLog {
  id: number;
  issueId: number;
  user: User;
  action: string;
  changes: string;  // Changed from details to changes to match backend
  timestamp: string;  // Changed from createdAt to timestamp to match backend
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  number: number;  // Added: current page number
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
