import "server-only";

/**
 * Server-only configuration. API_BASE_URL has no NEXT_PUBLIC_ prefix on
 * purpose: the browser never calls Spring directly, so it never needs - or
 * gets bundled with - the backend's address.
 *
 * Read on each call rather than at import, so `next build` doesn't require
 * the variable just to compile the route handlers.
 */
export function getApiBaseUrl(): string {
  const apiBaseUrl = process.env.API_BASE_URL;
  if (!apiBaseUrl) {
    throw new Error(
      "API_BASE_URL is not set. Copy frontend/.env.example to .env.local.",
    );
  }
  return apiBaseUrl;
}
