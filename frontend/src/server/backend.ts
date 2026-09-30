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

// Never cached: every answer is per-user.
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

// Compare Origin to the Host header, not nextUrl: nextUrl ignores Host (broke dulsara.localhost).
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

// cache-control (no-transform) and x-accel-buffering keep the stream unbuffered.
const RELAYED_HEADERS = ["content-type", "cache-control", "x-accel-buffering"];

// Body is streamed, never buffered, so a live SSE response flows straight through.
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
