import { DAY_MS } from "@/constants/timing";
import { DISPLAY_LOCALE } from "@/constants/ui";
import type { LocalDateTimeString } from "@/types/entities/primitives";

// No offset, so JS parses it as local time - which is intended; never treat it as UTC.
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

export const formatWeekday = (value: LocalDateTimeString) =>
  weekdayFormat.format(parseLocalDateTime(value)).toUpperCase();

export const formatDayOfMonth = (value: LocalDateTimeString) =>
  dayFormat.format(parseLocalDateTime(value));

export const formatTime = (value: LocalDateTimeString) =>
  timeFormat.format(parseLocalDateTime(value));

export const formatLongDate = (value: LocalDateTimeString) =>
  longDateFormat.format(parseLocalDateTime(value));

export function formatDaysAgo(
  value: LocalDateTimeString,
  now: Date = new Date(),
): string {
  const days = Math.round(
    (parseLocalDateTime(value).getTime() - now.getTime()) / DAY_MS,
  );
  return relativeFormat.format(days, "day");
}

export const isValidLocalDateTime = (value: string) =>
  !Number.isNaN(parseLocalDateTime(value).getTime());

const pad = (value: number) => String(value).padStart(2, "0");

// Not toISOString: that converts to UTC and shifts the hour.
export function toLocalDateTimeInput(date: Date): LocalDateTimeString {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function isWithinDays(
  value: LocalDateTimeString,
  days: number,
  now: Date = new Date(),
): boolean {
  const startsInMs = parseLocalDateTime(value).getTime() - now.getTime();
  return startsInMs >= 0 && startsInMs <= days * DAY_MS;
}
