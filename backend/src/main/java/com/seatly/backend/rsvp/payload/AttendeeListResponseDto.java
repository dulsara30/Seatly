package com.seatly.backend.rsvp.payload;

import java.util.List;

public record AttendeeListResponseDto(List<AttendeeResponseDto> items, long confirmedCount, int seatLimit) {
}
