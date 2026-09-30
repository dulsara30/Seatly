/**
 * A Java LocalDateTime as Jackson serialises it: ISO-8601 with no offset,
 * e.g. "2026-10-15T18:30:00". It is the organiser's wall-clock time, not an
 * instant - parse it as local time, never as UTC.
 */
export type LocalDateTimeString = string;

/** Java Long ids. Every id the backend issues fits well inside 2^53. */
export type Id = number;
