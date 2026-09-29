import type { NextRequest } from "next/server";
import { HttpStatus } from "@/constants/http";
import { BackendPaths } from "@/api/utils/BackendPaths";
import { setAuthCookie } from "@/server/authCookie";
import {
  JSON_CONTENT_TYPE,
  backendUnavailable,
  callBackend,
  errorResponse,
  isAllowedOrigin,
  relayBackendResponse,
  successResponse,
} from "@/server/backend";
import type { ApiEnvelope } from "@/types/responses/ApiEnvelope";
import type { BackendAuthResponse, SessionResponse } from "@/types/responses/AuthResponse";

/**
 * Login is the one call that can't go through the generic proxy: Spring
 * returns the token in the JSON body, and it has to be moved into the
 * httpOnly cookie before anything reaches the browser. The browser gets the
 * user only — the token never exists in page JavaScript.
 */
export async function POST(request: NextRequest) {
  if (!isAllowedOrigin(request)) {
    return errorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED");
  }

  let backendResponse: Response;
  try {
    backendResponse = await callBackend(BackendPaths.auth.login, {
      method: "POST",
      headers: { "content-type": JSON_CONTENT_TYPE, Accept: JSON_CONTENT_TYPE },
      body: await request.text(),
    });
  } catch {
    return backendUnavailable();
  }

  // Wrong credentials, validation errors: Spring's envelope, passed through.
  if (!backendResponse.ok) {
    return relayBackendResponse(backendResponse);
  }

  // Trusted: this is our own backend's documented response shape.
  const envelope: ApiEnvelope<BackendAuthResponse> = await backendResponse.json();
  const [auth] = envelope.results;
  if (auth === undefined) {
    return errorResponse(HttpStatus.BAD_GATEWAY, "UNKNOWN_ERROR");
  }

  const session: SessionResponse = { user: auth.user };
  const response = successResponse(session);
  setAuthCookie(response, auth.accessToken);
  return response;
}
