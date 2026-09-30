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
import type {
  BackendAuthResponse,
  SessionResponse,
} from "@/types/responses/AuthResponse";

// Moves the token from Spring's body into the httpOnly cookie; the browser never sees it.
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

  if (!backendResponse.ok) {
    return relayBackendResponse(backendResponse);
  }

  const envelope: ApiEnvelope<BackendAuthResponse> =
    await backendResponse.json();
  const [auth] = envelope.results;
  if (auth === undefined) {
    return errorResponse(HttpStatus.BAD_GATEWAY, "UNKNOWN_ERROR");
  }

  const session: SessionResponse = { user: auth.user };
  const response = successResponse(session);
  setAuthCookie(response, auth.accessToken);
  return response;
}
