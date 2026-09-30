import Link from "next/link";
import { Avatar } from "@/components/atoms/Avatar";
import { Badge } from "@/components/atoms/Badge";
import { DateChip } from "@/components/atoms/DateChip";
import { SeatBar } from "@/components/atoms/SeatBar";
import { TagChip } from "@/components/atoms/TagChip";
import { Copy } from "@/constants/copy";
import { EventMode, RsvpStatus } from "@/types/entities/enums";
import type { EventResponse } from "@/types/responses/EventResponse";
import { formatTime } from "@/utils/dateTime";

type EventCardProps = Pick<
  EventResponse,
  | "name"
  | "eventDate"
  | "mode"
  | "location"
  | "seatLimit"
  | "availableSeats"
  | "tags"
> & {
  hostName: string;
  /** The viewer's own RSVP, if any - shows GOING / WAITLISTED. */
  myRsvpStatus?: RsvpStatus;
  /** Absent for the create-form preview, which isn't a real event yet. */
  href?: string;
};

/**
 * The browse card. No cover image by design: the date chip anchors the card
 * and the seat bar is the second thing the eye lands on. Built from plain
 * fields (not a whole EventResponse) so the create form can preview a draft.
 */
export function EventCard({
  hostName,
  myRsvpStatus,
  href,
  ...event
}: EventCardProps) {
  const where =
    event.mode === EventMode.ONLINE ? Copy.detail.online : event.location;

  const body = (
    <article className="flex h-full flex-col gap-4 rounded-lg border border-gray-200 bg-white p-5 transition-colors duration-150 group-hover:border-black">
      <div className="flex items-start justify-between gap-3">
        <DateChip date={event.eventDate} />
        <div className="flex flex-col items-end gap-1.5">
          <Badge variant={event.mode} />
          {myRsvpStatus === RsvpStatus.CONFIRMED && <Badge variant="GOING" />}
          {myRsvpStatus === RsvpStatus.WAITLISTED && (
            <Badge variant="WAITLISTED" />
          )}
        </div>
      </div>

      <div className="flex flex-col gap-1">
        <h3 className="text-heading-s text-black">{event.name}</h3>
        <p className="text-body-s text-gray-500">
          {formatTime(event.eventDate)}
          {where && ` · ${where}`}
        </p>
      </div>

      {event.tags.length > 0 && (
        <ul className="flex flex-wrap gap-1.5">
          {event.tags.map((tag) => (
            <li key={tag.id}>
              <TagChip label={Copy.tag(tag.name)} />
            </li>
          ))}
        </ul>
      )}

      <div className="mt-auto flex flex-col gap-4 border-t border-gray-100 pt-4">
        <SeatBar
          availableSeats={event.availableSeats}
          seatLimit={event.seatLimit}
        />
        <div className="flex items-center gap-2">
          <Avatar name={hostName} size="small" />
          <span className="text-caption text-gray-500">
            {Copy.common.hostedBy(hostName)}
          </span>
        </div>
      </div>
    </article>
  );

  if (!href) {
    return body;
  }
  return (
    <Link
      href={href}
      className="group block h-full rounded-lg focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-black">
      {body}
    </Link>
  );
}
