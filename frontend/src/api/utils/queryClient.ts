import { QueryClient, isServer } from "@tanstack/react-query";
import { ApiError } from "@/api/utils/ApiError";
import {
  QUERY_GC_TIME_MS,
  QUERY_MAX_RETRIES,
  QUERY_STALE_TIME_MS,
} from "@/constants/timing";

// A 4xx means the request itself is wrong (not found, not allowed, invalid) -
// asking again gets the same answer, just later. Only a server-side failure
// or a dropped connection is worth another try.
function isWorthRetrying(error: Error): boolean {
  return !(
    error instanceof ApiError &&
    error.status !== null &&
    error.status < 500
  );
}

function makeQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: QUERY_STALE_TIME_MS,
        gcTime: QUERY_GC_TIME_MS,
        retry: (failureCount, error) =>
          isWorthRetrying(error) && failureCount < QUERY_MAX_RETRIES,
      },
      mutations: {
        // Never replay a write: POST /rsvp twice is two RSVPs attempted.
        retry: false,
      },
    },
  });
}

let browserQueryClient: QueryClient | undefined;

/**
 * TanStack's recommended App Router setup: one client per request on the
 * server, so one user's data can never leak into another's render; one
 * client for the lifetime of the tab in the browser. The axios 401 handler
 * uses the same browser client to clear the cache on session expiry.
 */
export function getQueryClient(): QueryClient {
  if (isServer) {
    return makeQueryClient();
  }
  browserQueryClient ??= makeQueryClient();
  return browserQueryClient;
}
