import { DAY_MS } from "@/constants/timing";
import { DISPLAY_LOCALE } from "@/constants/ui";
import type { LocalDateTimeString } from "@/types/entities/primitives";

/**
 * The backend sends LocalDateTime - the organiser's wall-clock time with no
 * offset ("2026-10-15T18:30:00"). ECMAScript parses a date-time string with
 * no offset as LOCAL time, which is exactly the intent; it is never shifted
 * as if it were UTC.
 */
export function parseLocalDateTime(value: LocalDateTimeString): Date {
  return new Date(value);
}

const weekdayFormat = new Intl.DateTimeFormat(DISPLAY_LOCALE, {
  weekday: "short",
});
const dayFormat = new Intl.DateTimeFormat(DISPLAY_LOCALE, { day: "numeric" });
const timeFormat = new Intl.DateTimeFormat(DISPLAY_LOCALE, {
  hour: "numeric",
  minute: "2-digit",
  hour12: true,
});
const longDateFormat = new Intl.DateTimeFormat(DISPLAY_LOCALE, {
  weekday: "long",
  day: "numeric",
  month: "long",
  year: "numeric",
});
const relativeFormat = new Intl.RelativeTimeFormat(DISPLAY_LOCALE, {
  numeric: "auto",
});

/** "THU" - for the date chip. */
export const formatWeekday = (value: LocalDateTimeString) =>
  weekdayFormat.format(parseLocalDateTime(value)).toUpperCase();

/** "15" - for the date chip. */
export const formatDayOfMonth = (value: LocalDateTimeString) =>
  dayFormat.format(parseLocalDateTime(value));

/** "6:30 pm" */
export const formatTime = (value: LocalDateTimeString) =>
  timeFormat.format(parseLocalDateTime(value));

/** "Thursday 15 October 2026" */
export const formatLongDate = (value: LocalDateTimeString) =>
  longDateFormat.format(parseLocalDateTime(value));

/** "2 days ago", "yesterday", "today" - whole days only; RSVP times don't need more. */
export function formatDaysAgo(
  value: LocalDateTimeString,
  now: Date = new Date(),
): string {
  const days = Math.round(
    (parseLocalDateTime(value).getTime() - now.getTime()) / DAY_MS,
  );
  return relativeFormat.format(days, "day");
}

/** Whether a string from a datetime-local input is a real date (it's "" while cleared). */
export const isValidLocalDateTime = (value: string) =>
  !Number.isNaN(parseLocalDateTime(value).getTime());

const pad = (value: number) => String(value).padStart(2, "0");

/**
 * A Date as the "YYYY-MM-DDTHH:mm" a datetime-local input expects, in local
 * time. (toISOString would convert to UTC and shift the hour.)
 */
export function toLocalDateTimeInput(date: Date): LocalDateTimeString {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

/** True if the event starts between now and `days` from now. */
export function isWithinDays(
  value: LocalDateTimeString,
  days: number,
  now: Date = new Date(),
): boolean {
  const startsInMs = parseLocalDateTime(value).getTime() - now.getTime();
  return startsInMs >= 0 && startsInMs <= days * DAY_MS;
}
