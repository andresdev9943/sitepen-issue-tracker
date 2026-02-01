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
  createdBy: User;
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
  user: User;
  content: string;
  createdAt: string;
}

export interface CreateCommentRequest {
  content: string;
}

export interface ActivityLog {
  id: number;
  issueId: number;
  user: User;
  action: string;
  details: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
