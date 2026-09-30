import { useMemo } from "react";
import { useSession } from "@/api/AuthApi";
import { useMyRsvps } from "@/api/RsvpApi";
import type { Id } from "@/types/entities/primitives";
import type { MyRsvpResponse } from "@/types/responses/RsvpResponse";

/**
 * The caller's active RSVPs, keyed by event - so any card or panel can ask
 * "am I going to this?" without a request per event.
 *
 * The event responses don't say whether YOU are going (the detail DTO has no
 * myRsvpStatus field), so this derives it from GET /v1/rsvps/my. Only fetched
 * when logged in: for an anonymous visitor the answer is simply "no".
 */
export function useMyRsvpLookup() {
  const { data: session } = useSession();
  const signedIn = session?.user != null;
  const myRsvps = useMyRsvps({ enabled: signedIn });

  const byEventId = useMemo(
    () =>
      new Map<Id, MyRsvpResponse>(
        myRsvps.data?.map((rsvp) => [rsvp.eventId, rsvp]),
      ),
    [myRsvps.data],
  );

  return { byEventId, isLoading: signedIn && myRsvps.isPending };
}
