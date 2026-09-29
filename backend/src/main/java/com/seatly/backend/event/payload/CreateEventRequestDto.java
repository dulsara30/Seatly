package com.seatly.backend.event.payload;

import com.seatly.backend.event.model.EventFieldLimits;
import com.seatly.backend.event.type.EventMessageKeys;
import com.seatly.backend.event.type.EventMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Shape validation only. Rules that depend on another field or on the current
 * time — ONLINE needs a meeting link, PHYSICAL needs a location, the date must
 * be in the future — are business validation and live in EventServiceImpl.
 */
public record CreateEventRequestDto(

        @NotBlank(message = EventMessageKeys.NAME_REQUIRED)
        @Size(max = EventFieldLimits.NAME_MAX_LENGTH, message = EventMessageKeys.NAME_TOO_LONG)
        String name,

        @NotBlank(message = EventMessageKeys.DESCRIPTION_REQUIRED)
        String description,

        @NotNull(message = EventMessageKeys.MODE_REQUIRED)
        EventMode mode,

        @Size(max = EventFieldLimits.LOCATION_MAX_LENGTH, message = EventMessageKeys.LOCATION_TOO_LONG)
        String location,

        @Size(max = EventFieldLimits.MEETING_LINK_MAX_LENGTH, message = EventMessageKeys.MEETING_LINK_TOO_LONG)
        String meetingLink,

        @NotNull(message = EventMessageKeys.DATE_REQUIRED)
        LocalDateTime eventDate,

        @NotNull(message = EventMessageKeys.SEAT_LIMIT_REQUIRED)
        @Min(value = EventFieldLimits.SEAT_LIMIT_MIN, message = EventMessageKeys.SEAT_LIMIT_MIN)
        Integer seatLimit,

        @NotNull(message = EventMessageKeys.TAG_IDS_REQUIRED)
        Set<@NotNull(message = EventMessageKeys.TAG_NOT_FOUND) Long> tagIds) {
}
