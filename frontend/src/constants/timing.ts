const SECOND_MS = 1000;
const MINUTE_MS = 60 * SECOND_MS;

export const SEARCH_DEBOUNCE_MS = 300;

// Short because seat counts change on every RSVP; our own mutations invalidate immediately.
export const QUERY_STALE_TIME_MS = 30 * SECOND_MS;

export const QUERY_GC_TIME_MS = 5 * MINUTE_MS;

export const QUERY_MAX_RETRIES = 2;

export const TOAST_DURATION_MS = 4 * SECOND_MS;

export const DAY_MS = 24 * 60 * MINUTE_MS;
