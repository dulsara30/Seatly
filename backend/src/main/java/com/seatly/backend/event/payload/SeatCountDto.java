package com.seatly.backend.event.payload;

/**
 * An event's seat counts at one moment — what live seat updates carry.
 * seatLimit isn't sent: it's availableSeats + confirmedCount, and sending it
 * as well would be a second source for the same number.
 */
public record SeatCountDto(Long eventId, long availableSeats, long confirmedCount) {
}
