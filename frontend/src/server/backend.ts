import "server-only";
import { HttpStatus } from "@/constants/http";
import { NextResponse, type NextRequest } from "next/server";
import { getApiBaseUrl } from "@/server/serverEnv";
import type { MessageKey } from "@/constants/messages";
import type {
  ApiEnvelope,
  ApiErrorEnvelope,
} from "@/types/responses/ApiEnvelope";

const SAFE_METHODS = new Set(["GET", "HEAD"]);

export const JSON_CONTENT_TYPE = "application/json";

/** A request from the Next server to Spring. Never cached: every answer is per-user. */
export function callBackend(
  backendPath: string,
  init: RequestInit,
): Promise<Response> {
  return fetch(new URL(backendPath, getApiBaseUrl()), {
    ...init,
    cache: "no-store",
    redirect: "manual",
  });
}

/**
 * CSRF check for anything that changes state. Browsers always send an Origin
 * header on a cross-site POST/PATCH/DELETE; if its host isn't the host this
 * request was sent to, another site is trying to act with the user's cookie.
 *
 * Compared against the Host header, NOT request.nextUrl: Next builds nextUrl
 * from its own configured hostname (localhost) and ignores Host, so on any
 * other name - dulsara.localhost, a LAN IP, a real domain - nextUrl never
 * matched and every write was refused. Both headers are set by the browser
 * itself; a page on another site can change neither.
 */
export function isAllowedOrigin(request: NextRequest): boolean {
  if (SAFE_METHODS.has(request.method)) {
    return true;
  }
  const origin = request.headers.get("origin");
  const host = request.headers.get("host");
  if (!origin || !host) {
    return false;
  }
  return URL.canParse(origin) && new URL(origin).host === host;
}

/**
 * Response headers worth keeping on the way through. cache-control carries
 * the stream's no-transform (stops gzip buffering it); x-accel-buffering
 * stops an Nginx in front of Next from buffering it.
 */
const RELAYED_HEADERS = ["content-type", "cache-control", "x-accel-buffering"];

/**
 * Spring's response, passed to the browser unchanged: status, a streamed body
 * (never buffered — a live SSE response flows straight through) and headers.
 */
export function relayBackendResponse(backendResponse: Response): NextResponse {
  const headers = new Headers();
  for (const name of RELAYED_HEADERS) {
    const value = backendResponse.headers.get(name);
    if (value) {
      headers.set(name, value);
    }
  }
  return new NextResponse(backendResponse.body, {
    status: backendResponse.status,
    headers,
  });
}

/** Errors raised by the Next layer itself, in the same envelope Spring uses. */
export function errorResponse(status: number, key: MessageKey): NextResponse {
  const body: ApiErrorEnvelope = {
    status: "unsuccessful",
    results: [{ message: key }],
  };
  return NextResponse.json(body, { status });
}

export function backendUnavailable(): NextResponse {
  return errorResponse(HttpStatus.BAD_GATEWAY, "SERVICE_UNAVAILABLE");
}

export function successResponse<T>(result: T): NextResponse {
  const body: ApiEnvelope<T> = { status: "successful", results: [result] };
  return NextResponse.json(body);
}
