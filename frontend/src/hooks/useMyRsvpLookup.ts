import { useMemo } from "react";
import { useSession } from "@/api/AuthApi";
import { useMyRsvps } from "@/api/RsvpApi";
import type { Id } from "@/types/entities/primitives";
import type { MyRsvpResponse } from "@/types/responses/RsvpResponse";

// Event responses don't say whether you're going, so derive it from GET /v1/rsvps/my.
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
