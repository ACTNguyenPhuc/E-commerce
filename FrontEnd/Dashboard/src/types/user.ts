export type Role = "admin" | "staff" | "customer";
export type UserStatus = "active" | "inactive" | "banned";

export interface User {
  id: number;
  email: string;
  phone: string | null;
  fullName: string;
  avatarUrl: string | null;
  useFileUpload?: boolean;
  gender?: "male" | "female" | "other" | null;
  dateOfBirth?: string | null;
  role: Role;
  status: UserStatus;
  createdAt: string;
  emailVerifiedAt: string | null;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface MeUpdate {
  fullName?: string;
  phone?: string;
  gender?: "male" | "female" | "other" | null;
  dateOfBirth?: string | null;
  avatarUrl?: string;
  useFileUpload?: boolean;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}
