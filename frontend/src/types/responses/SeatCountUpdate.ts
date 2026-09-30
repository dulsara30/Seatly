import type { Id } from "@/types/entities/primitives";

/**
 * event/payload/SeatCountDto.java — one live seat-update message. seatLimit
 * isn't sent: it's availableSeats + confirmedCount.
 */
export interface SeatCountUpdate {
  eventId: Id;
  availableSeats: number;
  confirmedCount: number;
}
