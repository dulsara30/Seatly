import { Avatar } from "@/components/atoms/Avatar";
import { Copy } from "@/constants/copy";
import type { WaitlistEntryResponse } from "@/types/responses/RsvpResponse";
import { formatDaysAgo } from "@/utils/dateTime";

type WaitlistRowProps = Pick<
  WaitlistEntryResponse,
  "position" | "name" | "email" | "rsvpAt"
>;

// Deliberate exception to the three-yellow-fills rule: organisers scan for the position.
export function WaitlistRow({
  position,
  name,
  email,
  rsvpAt,
}: WaitlistRowProps) {
  return (
    <li className="flex items-center gap-3 border-b border-gray-100 py-3 last:border-b-0">
      <span className="flex size-10 shrink-0 items-center justify-center rounded-full bg-yellow-500 text-heading-s text-black">
        {position}
      </span>
      <Avatar name={name} />
      <div className="flex min-w-0 flex-1 flex-col">
        <span className="truncate text-label-m text-black">{name}</span>
        <span className="truncate text-caption text-gray-500">{email}</span>
      </div>
      <span className="shrink-0 text-caption text-gray-400">
        {Copy.manage.joined(formatDaysAgo(rsvpAt))}
      </span>
    </li>
  );
}
