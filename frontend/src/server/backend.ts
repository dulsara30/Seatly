import "server-only";
import { HttpStatus } from "@/constants/http";
import { NextResponse, type NextRequest } from "next/server";
import { getApiBaseUrl } from "@/server/serverEnv";
import type { MessageKey } from "@/constants/messages";
import type { ApiEnvelope, ApiErrorEnvelope } from "@/types/responses/ApiEnvelope";

const SAFE_METHODS = new Set(["GET", "HEAD"]);

export const JSON_CONTENT_TYPE = "application/json";

/** A request from the Next server to Spring. Never cached: every answer is per-user. */
export function callBackend(backendPath: string, init: RequestInit): Promise<Response> {
  return fetch(new URL(backendPath, getApiBaseUrl()), { ...init, cache: "no-store", redirect: "manual" });
}

/**
 * CSRF check for anything that changes state. Browsers always send an Origin
 * header on a cross-site POST/PATCH/DELETE; if it isn't this app's own
 * origin, another site is trying to act with the user's cookie.
 */
export function isAllowedOrigin(request: NextRequest): boolean {
  return SAFE_METHODS.has(request.method) || request.headers.get("origin") === request.nextUrl.origin;
}

/** Spring's response, passed to the browser unchanged: status, body and content type. */
export function relayBackendResponse(backendResponse: Response): NextResponse {
  const headers = new Headers();
  const contentType = backendResponse.headers.get("content-type");
  if (contentType) {
    headers.set("content-type", contentType);
  }
  return new NextResponse(backendResponse.body, { status: backendResponse.status, headers });
}

/** Errors raised by the Next layer itself, in the same envelope Spring uses. */
export function errorResponse(status: number, key: MessageKey): NextResponse {
  const body: ApiErrorEnvelope = { status: "unsuccessful", results: [{ message: key }] };
  return NextResponse.json(body, { status });
}

export function backendUnavailable(): NextResponse {
  return errorResponse(HttpStatus.BAD_GATEWAY, "SERVICE_UNAVAILABLE");
}

export function successResponse<T>(result: T): NextResponse {
  const body: ApiEnvelope<T> = { status: "successful", results: [result] };
  return NextResponse.json(body);
}
