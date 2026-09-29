package com.seatly.backend.rsvp.payload;

import java.time.LocalDateTime;

/** Organiser-only, so it carries email — the organiser needs to reach attendees. */
public record AttendeeResponseDto(Long userId, String name, String email, LocalDateTime rsvpAt) {
}
