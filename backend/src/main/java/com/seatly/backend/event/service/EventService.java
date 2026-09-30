package com.seatly.backend.event.service;

import com.seatly.backend.common.payload.PageDto;
import com.seatly.backend.event.payload.CreateEventRequestDto;
import com.seatly.backend.event.payload.EventDetailResponseDto;
import com.seatly.backend.event.payload.EventFilterDto;
import com.seatly.backend.event.payload.EventResponseDto;
import com.seatly.backend.event.payload.SeatCountDto;
import com.seatly.backend.event.payload.UpdateEventRequestDto;
import java.util.List;

public interface EventService {

    EventDetailResponseDto createEvent(CreateEventRequestDto request);

    PageDto<EventResponseDto> getUpcomingEvents(EventFilterDto filter, int page, int size);

    EventDetailResponseDto getEventById(Long eventId);

    EventDetailResponseDto updateEvent(Long eventId, UpdateEventRequestDto request);

    List<EventResponseDto> getMyEvents();

    EventDetailResponseDto cancelEvent(Long eventId);

    SeatCountDto getSeatCount(Long eventId);
}
