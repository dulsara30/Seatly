import Link from "next/link";
import type { ReactNode } from "react";
import { DateChip } from "@/components/atoms/DateChip";
import type { LocalDateTimeString } from "@/types/entities/primitives";

interface EventListRowProps {
  date: LocalDateTimeString;
  title: string;
  meta: string;
  href: string;
  trailing: ReactNode;
}

export function EventListRow({
  date,
  title,
  meta,
  href,
  trailing,
}: EventListRowProps) {
  return (
    <li className="flex flex-col gap-3 border-b border-gray-100 py-4 last:border-b-0 sm:flex-row sm:items-center">
      <div className="flex min-w-0 flex-1 items-center gap-3">
        <DateChip date={date} />
        <div className="flex min-w-0 flex-col">
          <Link
            href={href}
            className="truncate text-label-m text-black hover:underline">
            {title}
          </Link>
          <span className="truncate text-caption text-gray-500">{meta}</span>
        </div>
      </div>
      <div className="flex shrink-0 flex-wrap items-center gap-3">
        {trailing}
      </div>
    </li>
  );
}
