import type { NextRequest } from "next/server";
import { HttpStatus } from "@/constants/http";
import { BackendPaths } from "@/api/utils/BackendPaths";
import { AUTH_COOKIE_NAME, clearAuthCookie } from "@/server/authCookie";
import {
  JSON_CONTENT_TYPE,
  backendUnavailable,
  callBackend,
  errorResponse,
  relayBackendResponse,
  successResponse,
} from "@/server/backend";
import type { ApiEnvelope } from "@/types/responses/ApiEnvelope";
import type { SessionResponse } from "@/types/responses/AuthResponse";
import type { User } from "@/types/entities/User";

// Logged out is a normal answer: { user: null } with a 200, not a 401.
export async function GET(request: NextRequest) {
  const token = request.cookies.get(AUTH_COOKIE_NAME)?.value;
  if (!token) {
    return anonymous();
  }

  let backendResponse: Response;
  try {
    backendResponse = await callBackend(BackendPaths.auth.me, {
      headers: { Accept: JSON_CONTENT_TYPE, Authorization: `Bearer ${token}` },
    });
  } catch {
    return backendUnavailable();
  }

  if (backendResponse.status === HttpStatus.UNAUTHORIZED) {
    const response = anonymous();
    clearAuthCookie(response);
    return response;
  }
  if (!backendResponse.ok) {
    return relayBackendResponse(backendResponse);
  }

  const envelope: ApiEnvelope<User> = await backendResponse.json();
  const [user] = envelope.results;
  if (user === undefined) {
    return errorResponse(HttpStatus.BAD_GATEWAY, "UNKNOWN_ERROR");
  }
  const session: SessionResponse = { user };
  return successResponse(session);
}

function anonymous() {
  const session: SessionResponse = { user: null };
  return successResponse(session);
}
