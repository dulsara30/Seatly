package com.seatly.backend.event.payload;

import com.seatly.backend.event.type.EventMode;

/**
 * Optional list filters. A null field means "don't filter on this". tag is
 * matched exactly against the stored name, so the service normalises it
 * before it reaches the repository.
 */
public record EventFilterDto(String tag, EventMode mode, String search) {
}
