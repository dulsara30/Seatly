import type { User } from "@/types/entities/User";

// Server-only: the login route moves accessToken into the cookie. Never import in browser code.
export interface BackendAuthResponse {
  accessToken: string;
  user: User;
}

// user is null when logged out - a normal state, not an error.
export interface SessionResponse {
  user: User | null;
}
