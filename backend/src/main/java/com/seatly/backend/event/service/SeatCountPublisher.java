package com.seatly.backend.event.service;

import com.seatly.backend.common.util.SeatUtils;
import com.seatly.backend.event.model.Event;
import com.seatly.backend.event.payload.SeatCountChangedEvent;
import com.seatly.backend.event.payload.SeatCountDto;
import com.seatly.backend.rsvp.repository.RsvpDao;
import com.seatly.backend.rsvp.type.RsvpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SeatCountPublisher {

    private final RsvpDao rsvpDao;
    private final ApplicationEventPublisher applicationEventPublisher;

    // Runs in the caller's transaction, so it flushes and counts pending changes.
    public SeatCountDto snapshot(Event event) {
        long confirmedCount = rsvpDao.countByEventIdAndStatus(event.getId(), RsvpStatus.CONFIRMED);
        return new SeatCountDto(event.getId(), SeatUtils.availableSeats(event.getSeatLimit(), confirmedCount),
                confirmedCount);
    }

    // MANDATORY: the listener is AFTER_COMMIT, so a publish with no transaction is never delivered.
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishChange(Event lockedEvent) {
        applicationEventPublisher.publishEvent(new SeatCountChangedEvent(snapshot(lockedEvent)));
    }
}
