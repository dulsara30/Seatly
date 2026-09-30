import type { ReactNode } from "react";
import { Button, LinkButton } from "@/components/atoms/Button";
import { Icon } from "@/components/atoms/Icon";
import { SeatBar } from "@/components/atoms/SeatBar";
import { Copy } from "@/constants/copy";
import { EventMode } from "@/types/entities/enums";
import type { EventDetailResponse } from "@/types/responses/EventResponse";
import type { RsvpPanelState } from "@/utils/rsvpPanelState";
import { SeatState, seatStateOf } from "@/utils/seats";

type RsvpPanelProps = Pick<
  EventDetailResponse,
  "availableSeats" | "seatLimit" | "mode" | "meetingLink"
> & {
  state: RsvpPanelState;
  /** Shown beside the seat bar — e.g. a live-updates indicator. A slot, so the panel knows nothing of streams. */
  seatStatus?: ReactNode;
  signInHref: string;
  manageHref: string;
  isPending: boolean;
  onRsvp: () => void;
  onCancel: () => void;
};

/**
 * The detail page's call to action. Presentational: which state it's in is
 * decided by rsvpPanelStateOf, and the parent owns the mutations.
 */
export function RsvpPanel({
  state,
  availableSeats,
  seatLimit,
  mode,
  meetingLink,
  seatStatus,
  ...actions
}: RsvpPanelProps) {
  const isFull = seatStateOf(availableSeats, seatLimit) === SeatState.FULL;

  return (
    <aside className="flex flex-col gap-4 rounded-lg border border-gray-200 bg-white p-5">
      <div className="flex flex-col gap-2">
        <SeatBar availableSeats={availableSeats} seatLimit={seatLimit} />
        {seatStatus}
      </div>
      <PanelAction state={state} isFull={isFull} {...actions} />
      {mode === EventMode.ONLINE && (
        <MeetingLinkRow meetingLink={meetingLink} />
      )}
    </aside>
  );
}

type PanelActionProps = Omit<
  RsvpPanelProps,
  "availableSeats" | "seatLimit" | "mode" | "meetingLink" | "seatStatus"
> & {
  isFull: boolean;
};

function PanelAction({
  state,
  isFull,
  signInHref,
  manageHref,
  isPending,
  onRsvp,
  onCancel,
}: PanelActionProps) {
  switch (state.kind) {
    case "ANONYMOUS":
      return (
        <LinkButton href={signInHref} variant="accent" fullWidth>
          {Copy.detail.signInToRsvp}
        </LinkButton>
      );
    case "ORGANISER":
      return (
        <div className="flex flex-col gap-3">
          <p className="text-body-s text-gray-500">{Copy.detail.yourEvent}</p>
          <LinkButton href={manageHref} variant="primary" fullWidth>
            {Copy.detail.manageEvent}
          </LinkButton>
        </div>
      );
    case "CLOSED":
      return (
        <p className="rounded-md bg-gray-100 px-3 py-2 text-body-s text-gray-500">
          {Copy.detail.closed}
        </p>
      );
    case "CONFIRMED":
      return (
        <div className="flex flex-col gap-3">
          <p className="flex items-center gap-2 rounded-md bg-green-50 px-3 py-2 text-label-m text-green-700">
            <Icon name="check" />
            {Copy.detail.going}
          </p>
          <Button
            variant="secondary"
            fullWidth
            disabled={isPending}
            onClick={onCancel}>
            {Copy.detail.cancelRsvp}
          </Button>
        </div>
      );
    case "WAITLISTED":
      return (
        <div className="flex flex-col gap-3">
          <div className="flex flex-col gap-0.5 rounded-md bg-yellow-50 px-3 py-2">
            <span className="text-label-m text-yellow-700">
              {Copy.detail.waitlisted(state.position)}
            </span>
            <span className="text-caption text-yellow-700">
              {Copy.detail.ahead(state.position - 1)}
            </span>
          </div>
          <Button
            variant="secondary"
            fullWidth
            disabled={isPending}
            onClick={onCancel}>
            {Copy.detail.leaveWaitlist}
          </Button>
        </div>
      );
    case "OPEN":
      // A free seat gets the one yellow CTA on the page; a full event's
      // "Join waitlist" is black - still an action, but not the headline one.
      return (
        <Button
          variant={isFull ? "primary" : "accent"}
          fullWidth
          disabled={isPending}
          onClick={onRsvp}>
          {isFull ? Copy.detail.joinWaitlist : Copy.detail.rsvp}
        </Button>
      );
  }
}

/** Visible only when the backend sent it - i.e. to the organiser and confirmed attendees. */
function MeetingLinkRow({ meetingLink }: { meetingLink: string | null }) {
  if (meetingLink === null) {
    return (
      <p className="flex items-center gap-2 rounded-md bg-gray-50 px-3 py-2 text-caption text-gray-500">
        <Icon name="lock" />
        {Copy.detail.linkLocked}
      </p>
    );
  }
  return (
    <a
      href={meetingLink}
      target="_blank"
      rel="noopener noreferrer"
      className="flex flex-col gap-0.5 rounded-md border border-gray-200 px-3 py-2 hover:border-black">
      <span className="text-label-s text-black">{Copy.detail.joinMeeting}</span>
      <span className="truncate text-caption text-gray-500">{meetingLink}</span>
    </a>
  );
}
