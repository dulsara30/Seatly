import { Copy } from "@/constants/copy";
import { classNames } from "@/utils/classNames";
import { SeatState, seatStateOf, takenSeats } from "@/utils/seats";

interface SeatBarProps {
  availableSeats: number;
  seatLimit: number;
}

const PERCENT = 100;

const LABEL_CLASSES: Record<SeatState, string> = {
  AVAILABLE: "text-black",
  LOW: "text-yellow-700",
  FULL: "text-black",
};

// Full turns the whole bar black so "no seats" reads as an end state, not a very full bar.
const FILL_CLASSES: Record<SeatState, string> = {
  AVAILABLE: "bg-yellow-500",
  LOW: "bg-yellow-500",
  FULL: "bg-black",
};

function labelFor(
  state: SeatState,
  availableSeats: number,
  seatLimit: number,
): string {
  switch (state) {
    case SeatState.FULL:
      return Copy.seats.full;
    case SeatState.LOW:
      return Copy.seats.onlyLeft(availableSeats);
    case SeatState.AVAILABLE:
      return Copy.seats.left(availableSeats, seatLimit);
  }
}

// The bar shows seats TAKEN, so it fills up as the event does.
export function SeatBar({ availableSeats, seatLimit }: SeatBarProps) {
  const state = seatStateOf(availableSeats, seatLimit);
  const taken = takenSeats(availableSeats, seatLimit);
  const filledPercent =
    state === SeatState.FULL
      ? PERCENT
      : Math.round((taken / seatLimit) * PERCENT);

  return (
    <div className="flex flex-col gap-1.5">
      <div className="flex items-baseline justify-between gap-2">
        <span className={classNames("text-label-s", LABEL_CLASSES[state])}>
          {labelFor(state, availableSeats, seatLimit)}
        </span>
        <span className="text-caption text-gray-400">
          {Copy.seats.taken(taken, seatLimit)}
        </span>
      </div>
      <div
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={seatLimit}
        aria-valuenow={taken}
        aria-label={Copy.seats.progressLabel(availableSeats, seatLimit)}
        className="h-1.5 overflow-hidden rounded-full bg-gray-100">
        {/* The width is data, not design - the one value that must be inline. */}
        <div
          className={classNames(
            "h-full rounded-full transition-all duration-300",
            FILL_CLASSES[state],
          )}
          style={{ width: `${filledPercent}%` }}
        />
      </div>
    </div>
  );
}
