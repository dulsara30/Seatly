import type { User } from "@/types/entities/User";

/**
 * auth/payload/AuthResponseDto.java - what the backend's login returns.
 * Only the Next server ever sees this: the login route handler moves
 * accessToken into an httpOnly cookie and passes the browser a
 * SessionResponse instead. Browser code must never import this type.
 */
export interface BackendAuthResponse {
  accessToken: string;
  user: User;
}

/**
 * Frontend-only: what the Next auth routes return to the browser. user is
 * null when there is no valid session - a normal state, not an error.
 */
export interface SessionResponse {
  user: User | null;
}
