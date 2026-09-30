import { Avatar } from "@/components/atoms/Avatar";
import { Copy } from "@/constants/copy";
import type { AttendeeResponse } from "@/types/responses/RsvpResponse";
import { formatDaysAgo } from "@/utils/dateTime";

type AttendeeRowProps = Pick<AttendeeResponse, "name" | "email" | "rsvpAt">;

export function AttendeeRow({ name, email, rsvpAt }: AttendeeRowProps) {
  return (
    <li className="flex items-center gap-3 border-b border-gray-100 py-3 last:border-b-0">
      <Avatar name={name} />
      <div className="flex min-w-0 flex-1 flex-col">
        <span className="truncate text-label-m text-black">{name}</span>
        <span className="truncate text-caption text-gray-500">{email}</span>
      </div>
      <span className="shrink-0 text-caption text-gray-400">{Copy.manage.rsvped(formatDaysAgo(rsvpAt))}</span>
    </li>
  );
}
