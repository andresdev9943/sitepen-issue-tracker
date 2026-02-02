import { User } from './user.model';

export interface AuthResponse {
  token: string;
  type: string;
  userId: string;  // UUID
  email: string;
  fullName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}
