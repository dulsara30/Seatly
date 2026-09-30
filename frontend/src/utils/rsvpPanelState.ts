import { EventStatus, RsvpStatus } from "@/types/entities/enums";
import type { User } from "@/types/entities/User";
import type { EventDetailResponse } from "@/types/responses/EventResponse";
import type { MyRsvpResponse } from "@/types/responses/RsvpResponse";

export type RsvpPanelState =
  | { kind: "ANONYMOUS" }
  | { kind: "ORGANISER" }
  | { kind: "CLOSED" }
  | { kind: "CONFIRMED" }
  | { kind: "WAITLISTED"; position: number }
  | { kind: "OPEN" };

// Order matters: own event, then a closed event, then an existing RSVP, then the RSVP button.
export function rsvpPanelStateOf(
  event: EventDetailResponse,
  viewer: User | null,
  myRsvp: MyRsvpResponse | undefined,
): RsvpPanelState {
  if (viewer === null) {
    return { kind: "ANONYMOUS" };
  }
  if (event.organizer.id === viewer.id) {
    return { kind: "ORGANISER" };
  }
  if (event.status !== EventStatus.UPCOMING) {
    return { kind: "CLOSED" };
  }
  if (myRsvp?.status === RsvpStatus.CONFIRMED) {
    return { kind: "CONFIRMED" };
  }
  if (myRsvp?.status === RsvpStatus.WAITLISTED && myRsvp.position !== null) {
    return { kind: "WAITLISTED", position: myRsvp.position };
  }
  return { kind: "OPEN" };
}
