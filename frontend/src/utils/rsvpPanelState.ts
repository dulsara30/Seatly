import { EventStatus, RsvpStatus } from "@/types/entities/enums";
import type { User } from "@/types/entities/User";
import type { EventDetailResponse } from "@/types/responses/EventResponse";
import type { MyRsvpResponse } from "@/types/responses/RsvpResponse";

/** Every state the RSVP panel can be in - one per distinct thing the viewer can do. */
export type RsvpPanelState =
  | { kind: "ANONYMOUS" }
  | { kind: "ORGANISER" }
  | { kind: "CLOSED" }
  | { kind: "CONFIRMED" }
  | { kind: "WAITLISTED"; position: number }
  | { kind: "OPEN" };

/**
 * Which panel to show. Order matters: your own event beats everything
 * (organisers can't RSVP to it), a closed event beats "you could RSVP", and
 * an existing RSVP beats the RSVP button.
 */
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
