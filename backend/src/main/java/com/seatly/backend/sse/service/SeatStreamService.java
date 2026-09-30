package com.seatly.backend.sse.service;

import com.seatly.backend.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SeatStreamService {

    private final EventService eventService;
    private final SeatStreamRegistry registry;

    /**
     * The counts are read first, so an unknown or deleted event is a normal
     * 404 before any stream exists — never an open stream for nothing.
     */
    public SseEmitter openSeatStream(Long eventId) {
        return registry.open(eventService.getSeatCount(eventId));
    }
}
