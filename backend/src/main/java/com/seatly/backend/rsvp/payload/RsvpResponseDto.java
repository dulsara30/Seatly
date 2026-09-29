package com.seatly.backend.rsvp.payload;

import com.seatly.backend.rsvp.type.RsvpStatus;

/** position is set only while WAITLISTED; 1 is next in line. */
public record RsvpResponseDto(Long eventId, RsvpStatus status, Integer position) {
}
