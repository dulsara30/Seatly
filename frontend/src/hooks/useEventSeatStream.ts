import { useEffect, useState } from "react";
import { useApplySeatUpdate } from "@/api/EventApi";
import { SEAT_UPDATE_EVENT } from "@/api/utils/BackendPaths";
import { eventSeatStreamUrl } from "@/api/utils/ApiEndpoints";
import type { Id } from "@/types/entities/primitives";
import type { SeatCountUpdate } from "@/types/responses/SeatCountUpdate";

export const SeatStreamStatus = {
  CONNECTING: "CONNECTING",
  LIVE: "LIVE",
  RECONNECTING: "RECONNECTING",
  UNAVAILABLE: "UNAVAILABLE",
} as const;
export type SeatStreamStatus = (typeof SeatStreamStatus)[keyof typeof SeatStreamStatus];

// EventSource reconnects by itself and the server resends the counts on every (re)connect.
export function useEventSeatStream(eventId: Id): SeatStreamStatus {
  const applySeatUpdate = useApplySeatUpdate();
  const [status, setStatus] = useState<SeatStreamStatus>(SeatStreamStatus.CONNECTING);

  useEffect(() => {
    const source = new EventSource(eventSeatStreamUrl(eventId));

    const onSeatUpdate = (message: MessageEvent<string>) => {
      const update: SeatCountUpdate = JSON.parse(message.data);
      applySeatUpdate(update);
      setStatus(SeatStreamStatus.LIVE);
    };

    source.addEventListener(SEAT_UPDATE_EVENT, onSeatUpdate);
    source.onopen = () => setStatus(SeatStreamStatus.LIVE);
    // CONNECTING after an error means the browser is retrying; CLOSED means it gave up.
    source.onerror = () =>
      setStatus(source.readyState === EventSource.CLOSED ? SeatStreamStatus.UNAVAILABLE : SeatStreamStatus.RECONNECTING);

    // Close on unmount: an open stream holds a Next connection and a Spring registry slot.
    return () => {
      source.removeEventListener(SEAT_UPDATE_EVENT, onSeatUpdate);
      source.close();
    };
  }, [eventId, applySeatUpdate]);

  return status;
}
