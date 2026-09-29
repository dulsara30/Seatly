package com.seatly.backend.event.payload;

import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.tag.payload.TagSummaryDto;
import com.seatly.backend.user.payload.UserSummaryDto;
import java.time.LocalDateTime;
import java.util.List;

/**
 * List-card shape. availableSeats is derived from a confirmed-RSVP count, not
 * stored on the event, so the service supplies it at mapping time.
 */
public record EventResponseDto(
        Long id,
        String name,
        LocalDateTime eventDate,
        EventMode mode,
        String location,
        Integer seatLimit,
        long availableSeats,
        EventStatus status,
        List<TagSummaryDto> tags,
        UserSummaryDto organizer) {
}
