import { Copy } from "@/constants/copy";
import { SeatStreamStatus } from "@/hooks/useEventSeatStream";
import { classNames } from "@/utils/classNames";

const DOT_CLASSES: Record<SeatStreamStatus, string> = {
  CONNECTING: "bg-gray-300",
  LIVE: "bg-green-700 animate-pulse",
  RECONNECTING: "bg-gray-400",
  UNAVAILABLE: "bg-gray-300",
};

const LABELS: Record<SeatStreamStatus, string> = {
  CONNECTING: Copy.live.connecting,
  LIVE: Copy.live.live,
  RECONNECTING: Copy.live.reconnecting,
  UNAVAILABLE: Copy.live.unavailable,
};

/**
 * Says whether the seat count is live. While reconnecting it says so in
 * words, so a dropped connection reads as "a moment behind", not broken.
 */
export function LiveIndicator({ status }: { status: SeatStreamStatus }) {
  return (
    <span role="status" className="inline-flex items-center gap-1.5 text-caption text-gray-500">
      <span aria-hidden="true" className={classNames("size-2 rounded-full", DOT_CLASSES[status])} />
      {LABELS[status]}
    </span>
  );
}
