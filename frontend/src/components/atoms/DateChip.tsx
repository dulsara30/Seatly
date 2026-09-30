import type { LocalDateTimeString } from "@/types/entities/primitives";
import {
  formatDayOfMonth,
  formatLongDate,
  formatWeekday,
} from "@/utils/dateTime";

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
