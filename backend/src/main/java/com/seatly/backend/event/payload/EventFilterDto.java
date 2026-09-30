package com.seatly.backend.event.payload;

import com.seatly.backend.event.type.EventMode;

public record EventFilterDto(String tag, EventMode mode, String search) {
}
