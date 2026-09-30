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

async function forwardToBackend(
  request: NextRequest,
  context: RouteContext<"/api/proxy/[...path]">,
) {
  if (!isAllowedOrigin(request)) {
    return errorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED");
  }

  // Re-encode each segment so a crafted path ("//host") can't send the token to another host.
  const { path } = await context.params;
  const backendPath = `/${path.map(encodeURIComponent).join("/")}`;
  if (!backendPath.startsWith(BACKEND_PATH_PREFIX)) {
    return errorResponse(HttpStatus.NOT_FOUND, "INVALID_PARAMETER");
  }

  const token = request.cookies.get(AUTH_COOKIE_NAME)?.value;
  // Forward the browser's Accept: the stream is text/event-stream and Spring 406s anything else.
  const headers = new Headers({ Accept: request.headers.get("accept") ?? JSON_CONTENT_TYPE });
  const contentType = request.headers.get("content-type");
  if (contentType) {
    headers.set("content-type", contentType);
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let backendResponse: Response;
  try {
    backendResponse = await callBackend(
      `${backendPath}${request.nextUrl.search}`,
      {
        method: request.method,
        headers,
        body: METHODS_WITHOUT_BODY.has(request.method)
          ? undefined
          : await request.text(),
        // Aborts upstream when the tab closes, so Spring frees the stream's registry slot.
        signal: request.signal,
      },
    );
  } catch {
    return backendUnavailable();
  }

  const response = relayBackendResponse(backendResponse);
  // Clear the dead cookie so proxy.ts stops treating this browser as logged in.
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
