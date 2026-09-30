import { LOW_SEATS_RATIO } from "@/constants/ui";

export const SeatState = {
  AVAILABLE: "AVAILABLE",
  LOW: "LOW",
  FULL: "FULL",
} as const;
export type SeatState = (typeof SeatState)[keyof typeof SeatState];

export function seatStateOf(availableSeats: number, seatLimit: number): SeatState {
  if (availableSeats <= 0) {
    return SeatState.FULL;
  }
  return availableSeats / seatLimit <= LOW_SEATS_RATIO ? SeatState.LOW : SeatState.AVAILABLE;
}

export const takenSeats = (availableSeats: number, seatLimit: number) => seatLimit - availableSeats;
