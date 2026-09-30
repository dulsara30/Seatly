package com.seatly.backend.rsvp.service;

import com.seatly.backend.common.util.SeatUtils;
import com.seatly.backend.event.model.Event;
import com.seatly.backend.event.service.SeatCountPublisher;
import com.seatly.backend.rsvp.model.Rsvp;
import com.seatly.backend.rsvp.repository.RsvpDao;
import com.seatly.backend.rsvp.type.RsvpStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WaitlistManager {

    private static final int FIRST_POSITION = 1;

    private final RsvpDao rsvpDao;
    private final SeatCountPublisher seatCountPublisher;

    // MANDATORY: the caller must hold the event row lock; this at least rules out no transaction.
    @Transactional(propagation = Propagation.MANDATORY)
    public void rebalance(Event lockedEvent) {
        long freeSeats = SeatUtils.availableSeats(lockedEvent.getSeatLimit(),
                rsvpDao.countByEventIdAndStatus(lockedEvent.getId(), RsvpStatus.CONFIRMED));
        List<Rsvp> queue = rsvpDao.findByEventIdAndStatusOrderByPositionAsc(lockedEvent.getId(), RsvpStatus.WAITLISTED);

        int promotions = Math.clamp(freeSeats, 0, queue.size());
        queue.subList(0, promotions).forEach(this::confirm);
        renumber(queue.subList(promotions, queue.size()));

        // Also published here so no caller can promote silently; the duplicate publish is harmless.
        if (promotions > 0) {
            seatCountPublisher.publishChange(lockedEvent);
        }
    }

    private void confirm(Rsvp rsvp) {
        rsvp.setStatus(RsvpStatus.CONFIRMED);
        rsvp.setPosition(null);
    }

    private void renumber(List<Rsvp> stillWaiting) {
        for (int index = 0; index < stillWaiting.size(); index++) {
            stillWaiting.get(index).setPosition(FIRST_POSITION + index);
        }
    }
}
