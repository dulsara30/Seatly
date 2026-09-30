package com.seatly.backend.event.payload;

public record SeatCountDto(Long eventId, long availableSeats, long confirmedCount) {
}
