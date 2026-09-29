package com.seatly.backend.rsvp.payload;

import com.seatly.backend.rsvp.type.RsvpStatus;
import java.time.LocalDateTime;

public record MyRsvpResponseDto(
        Long eventId,
        String eventName,
        LocalDateTime eventDate,
        RsvpStatus status,
        Integer position) {
}
