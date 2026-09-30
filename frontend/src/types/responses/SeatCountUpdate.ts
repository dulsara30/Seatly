import type { Id } from "@/types/entities/primitives";

// event/payload/SeatCountDto.java; seatLimit isn't sent (availableSeats + confirmedCount).
export interface SeatCountUpdate {
  eventId: Id;
  availableSeats: number;
  confirmedCount: number;
}
