import type { NextRequest } from "next/server";
import { HttpStatus } from "@/constants/http";
import { BACKEND_PATH_PREFIX } from "@/api/utils/BackendPaths";
import { AUTH_COOKIE_NAME, clearAuthCookie } from "@/server/authCookie";
import {
  JSON_CONTENT_TYPE,
  backendUnavailable,
  callBackend,
  errorResponse,
  isAllowedOrigin,
  relayBackendResponse,
} from "@/server/backend";

const METHODS_WITHOUT_BODY = new Set(["GET", "HEAD"]);

/**
 * /api/proxy/v1/... -> Spring /v1/..., with the session cookie turned into
 * "Authorization: Bearer <token>". This is the only place the token is read
 * for API calls, and it never leaves the server.
 */
async function forwardToBackend(request: NextRequest, context: RouteContext<"/api/proxy/[...path]">) {
  if (!isAllowedOrigin(request)) {
    return errorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED");
  }

  // Each segment is re-encoded so a crafted path can't escape the backend:
  // unencoded, a leading empty segment would produce "//host", which URL
  // resolution treats as a different server — the proxy would then send the
  // user's token wherever the attacker pointed it.
  const { path } = await context.params;
  const backendPath = `/${path.map(encodeURIComponent).join("/")}`;
  if (!backendPath.startsWith(BACKEND_PATH_PREFIX)) {
    return errorResponse(HttpStatus.NOT_FOUND, "INVALID_PARAMETER");
  }

  const token = request.cookies.get(AUTH_COOKIE_NAME)?.value;
  const headers = new Headers({ Accept: JSON_CONTENT_TYPE });
  const contentType = request.headers.get("content-type");
  if (contentType) {
    headers.set("content-type", contentType);
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let backendResponse: Response;
  try {
    backendResponse = await callBackend(`${backendPath}${request.nextUrl.search}`, {
      method: request.method,
      headers,
      body: METHODS_WITHOUT_BODY.has(request.method) ? undefined : await request.text(),
    });
  } catch {
    return backendUnavailable();
  }

  const response = relayBackendResponse(backendResponse);
  // Spring rejected the token (expired or revoked): drop the cookie, so the
  // route guard in proxy.ts stops treating this browser as logged in.
  if (backendResponse.status === HttpStatus.UNAUTHORIZED && token) {
    clearAuthCookie(response);
  }
  return response;
}

export const GET = forwardToBackend;
export const POST = forwardToBackend;
export const PUT = forwardToBackend;
export const PATCH = forwardToBackend;
export const DELETE = forwardToBackend;
