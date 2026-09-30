const SECOND_MS = 1000;
const MINUTE_MS = 60 * SECOND_MS;

export const SEARCH_DEBOUNCE_MS = 300;

/**
 * How long fetched data counts as fresh before TanStack Query refetches it on
 * the next use. Seat counts change whenever anyone RSVPs, so this is short;
 * our own mutations invalidate what they change immediately regardless.
 */
export const QUERY_STALE_TIME_MS = 30 * SECOND_MS;

/** How long unused cached data is kept before it's dropped from memory. */
export const QUERY_GC_TIME_MS = 5 * MINUTE_MS;

/** Retries for a failed query - only for 5xx/network failures, see queryClient.ts. */
export const QUERY_MAX_RETRIES = 2;

/** How long a toast stays on screen before dismissing itself. */
export const TOAST_DURATION_MS = 4 * SECOND_MS;

export const DAY_MS = 24 * 60 * MINUTE_MS;
