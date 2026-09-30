package com.seatly.backend.rsvp.payload;

import java.time.LocalDateTime;

public record AttendeeResponseDto(Long userId, String name, String email, LocalDateTime rsvpAt) {
}
