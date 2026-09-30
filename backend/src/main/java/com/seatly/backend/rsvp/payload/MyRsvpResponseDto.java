package com.seatly.backend.rsvp.payload;

import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import java.time.LocalDateTime;

/**
 * eventStatus is here because cancelling an event keeps its RSVP rows as they
 * were: without it, a cancelled event would still read as "Confirmed" on the
 * attendee's list.
 */
public record MyRsvpResponseDto(
        Long eventId,
        String eventName,
        LocalDateTime eventDate,
        EventStatus eventStatus,
        RsvpStatus status,
        Integer position) {
}
