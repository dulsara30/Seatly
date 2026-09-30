package com.seatly.backend.rsvp.payload;

import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import java.time.LocalDateTime;

// eventStatus: a cancelled event keeps its RSVP rows, which would still read as "Confirmed".
public record MyRsvpResponseDto(
        Long eventId,
        String eventName,
        LocalDateTime eventDate,
        EventStatus eventStatus,
        RsvpStatus status,
        Integer position) {
}
