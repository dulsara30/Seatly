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

/**
 * The one place seat counts are measured and announced. Every path that can
 * change them — RSVP created, RSVP cancelled, a waitlist promotion, a raised
 * seat limit — calls publishChange, so none of them can forget the count or
 * compute it differently.
 *
 * This is plain Spring eventing: nothing here knows that SSE, or any
 * transport, exists.
 */
@Component
@RequiredArgsConstructor
public class SeatCountPublisher {

    private final RsvpDao rsvpDao;
    private final ApplicationEventPublisher applicationEventPublisher;

    /** Current counts, read in the caller's transaction (flushing its pending changes first). */
    public SeatCountDto snapshot(Event event) {
        long confirmedCount = rsvpDao.countByEventIdAndStatus(event.getId(), RsvpStatus.CONFIRMED);
        return new SeatCountDto(event.getId(), SeatUtils.availableSeats(event.getSeatLimit(), confirmedCount),
                confirmedCount);
    }

    /**
     * Announces the counts as they stand now. MANDATORY: the listener fires
     * AFTER_COMMIT, so a publish with no transaction around it would never be
     * delivered at all — this makes that mistake an error instead of silence.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishChange(Event lockedEvent) {
        applicationEventPublisher.publishEvent(new SeatCountChangedEvent(snapshot(lockedEvent)));
    }
}
