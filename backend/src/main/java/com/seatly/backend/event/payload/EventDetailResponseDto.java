package com.seatly.backend.event.payload;

import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.tag.payload.TagSummaryDto;
import com.seatly.backend.user.payload.UserSummaryDto;
import java.time.LocalDateTime;
import java.util.List;

public record EventDetailResponseDto(
        Long id,
        String name,
        String description,
        LocalDateTime eventDate,
        EventMode mode,
        String location,
        String meetingLink,
        Integer seatLimit,
        long availableSeats,
        EventStatus status,
        List<TagSummaryDto> tags,
        UserSummaryDto organizer) {
}
