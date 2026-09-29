import type { NextRequest } from "next/server";
import { HttpStatus } from "@/constants/http";
import { clearAuthCookie } from "@/server/authCookie";
import { errorResponse, isAllowedOrigin, successResponse } from "@/server/backend";
import type { SessionResponse } from "@/types/responses/AuthResponse";

/**
 * Tokens are stateless, so Spring has nothing to revoke — logging out means
 * deleting the cookie that holds the token. Only the server can: it's httpOnly.
 */
export async function POST(request: NextRequest) {
  if (!isAllowedOrigin(request)) {
    return errorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED");
  }
  const session: SessionResponse = { user: null };
  const response = successResponse(session);
  clearAuthCookie(response);
  return response;
}
