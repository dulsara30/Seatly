package com.seatly.backend.rsvp.payload;

import java.time.LocalDateTime;

public record WaitlistEntryResponseDto(Long userId, String name, String email, Integer position,
        LocalDateTime rsvpAt) {
}
