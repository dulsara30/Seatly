package com.seatly.backend.event.payload;

import com.seatly.backend.event.model.EventFieldLimits;
import com.seatly.backend.event.type.EventMessageKeys;
import com.seatly.backend.event.type.EventMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

// Partial update: null = unchanged (so no required-field rules); empty tagIds removes all tags.
public record UpdateEventRequestDto(

        @Size(max = EventFieldLimits.NAME_MAX_LENGTH, message = EventMessageKeys.NAME_TOO_LONG)
        String name,

        String description,

        EventMode mode,

        @Size(max = EventFieldLimits.LOCATION_MAX_LENGTH, message = EventMessageKeys.LOCATION_TOO_LONG)
        String location,

        @Size(max = EventFieldLimits.MEETING_LINK_MAX_LENGTH, message = EventMessageKeys.MEETING_LINK_TOO_LONG)
        String meetingLink,

        LocalDateTime eventDate,

        @Min(value = EventFieldLimits.SEAT_LIMIT_MIN, message = EventMessageKeys.SEAT_LIMIT_MIN)
        Integer seatLimit,

        Set<@NotNull(message = EventMessageKeys.TAG_NOT_FOUND) Long> tagIds) {
}
