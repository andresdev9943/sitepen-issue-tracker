import { User } from './user.model';
import { ProjectRole } from './enums';

export interface ProjectMember {
  id: string;  // UUID
  user: User;
  role: ProjectRole;
  joinedAt: string;
}

export interface Project {
  id: string;  // UUID
  name: string;
  description: string;
  owner: User;
  createdAt: string;
  updatedAt: string;
  members: ProjectMember[];
  issueCount: number;
}

export interface CreateProjectRequest {
  name: string;
  description: string;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
}

export interface AddMemberRequest {
  email: string;
}
