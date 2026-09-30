import { useEffect, useState } from "react";
import { useApplySeatUpdate } from "@/api/EventApi";
import { SEAT_UPDATE_EVENT } from "@/api/utils/BackendPaths";
import { eventSeatStreamUrl } from "@/api/utils/ApiEndpoints";
import type { Id } from "@/types/entities/primitives";
import type { SeatCountUpdate } from "@/types/responses/SeatCountUpdate";

export const SeatStreamStatus = {
  /** Opening, before the first message. */
  CONNECTING: "CONNECTING",
  /** Receiving updates. */
  LIVE: "LIVE",
  /** Dropped; EventSource is retrying on its own. The count may be a moment behind. */
  RECONNECTING: "RECONNECTING",
  /** The server refused the stream (e.g. unknown event); EventSource won't retry. */
  UNAVAILABLE: "UNAVAILABLE",
} as const;
export type SeatStreamStatus = (typeof SeatStreamStatus)[keyof typeof SeatStreamStatus];

/**
 * Live seat counts for one event, over Server-Sent Events.
 *
 * EventSource is the browser's own client: no library, and it reconnects by
 * itself after a dropped connection. The server sends the current counts on
 * every (re)connect, so a gap never leaves a stale count behind.
 *
 * This hook owns the connection; what an update MEANS for the cache lives in
 * the API layer (useApplySeatUpdate) — components never touch queryClient.
 */
export function useEventSeatStream(eventId: Id): SeatStreamStatus {
  const applySeatUpdate = useApplySeatUpdate();
  const [status, setStatus] = useState<SeatStreamStatus>(SeatStreamStatus.CONNECTING);

  useEffect(() => {
    const source = new EventSource(eventSeatStreamUrl(eventId));

    const onSeatUpdate = (message: MessageEvent<string>) => {
      // Trusted: our own backend's SeatCountDto.
      const update: SeatCountUpdate = JSON.parse(message.data);
      applySeatUpdate(update);
      setStatus(SeatStreamStatus.LIVE);
    };

    source.addEventListener(SEAT_UPDATE_EVENT, onSeatUpdate);
    source.onopen = () => setStatus(SeatStreamStatus.LIVE);
    // CONNECTING after an error means the browser is retrying; CLOSED means
    // it gave up (a non-stream response, like a 404) and won't try again.
    source.onerror = () =>
      setStatus(source.readyState === EventSource.CLOSED ? SeatStreamStatus.UNAVAILABLE : SeatStreamStatus.RECONNECTING);

    // Closing on unmount matters: an open stream holds a connection on the
    // Next server and a registry slot on Spring until something closes it.
    return () => {
      source.removeEventListener(SEAT_UPDATE_EVENT, onSeatUpdate);
      source.close();
    };
  }, [eventId, applySeatUpdate]);

  return status;
}
