import { QueryClient, isServer } from "@tanstack/react-query";
import { ApiError } from "@/api/utils/ApiError";
import {
  QUERY_GC_TIME_MS,
  QUERY_MAX_RETRIES,
  QUERY_STALE_TIME_MS,
} from "@/constants/timing";

// Retry only 5xx/network failures: a 4xx would just get the same answer again.
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

// One client per request on the server (no cross-user leaks); one per tab in the browser.
export function getQueryClient(): QueryClient {
  if (isServer) {
    return makeQueryClient();
  }
  browserQueryClient ??= makeQueryClient();
  return browserQueryClient;
}
