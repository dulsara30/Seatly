import type { LocalDateTimeString } from "@/types/entities/primitives";
import {
  formatDayOfMonth,
  formatLongDate,
  formatWeekday,
} from "@/utils/dateTime";

/**
 * The card's anchor instead of a cover photo: consistent on every card,
 * on-brand, and it carries real information. Yellow here is text on black -
 * a label, not a filled surface.
 */
export function DateChip({ date }: { date: LocalDateTimeString }) {
  return (
    <time
      dateTime={date}
      title={formatLongDate(date)}
      className="flex size-12 shrink-0 flex-col items-center justify-center rounded-md bg-black">
      <span className="text-overline text-yellow-500">
        {formatWeekday(date)}
      </span>
      <span className="text-heading-m leading-none text-white">
        {formatDayOfMonth(date)}
      </span>
    </time>
  );
}
