package com.seatly.backend.rsvp.service;

import com.seatly.backend.common.util.SeatUtils;
import com.seatly.backend.event.model.Event;
import com.seatly.backend.rsvp.model.Rsvp;
import com.seatly.backend.rsvp.repository.RsvpDao;
import com.seatly.backend.rsvp.type.RsvpStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The single place the waitlist changes shape. Called after anything that can
 * free a seat or open a gap in the queue — a confirmed attendee cancelling, a
 * waitlisted one leaving, the organiser raising the seat limit — so all three
 * share one promotion rule and one renumbering rule.
 *
 * The caller must already hold the event row's FOR UPDATE lock. That lock is
 * what makes "count confirmed, promote that many" safe: nothing else can
 * confirm or cancel on this event until the caller commits. MANDATORY makes
 * calling this outside a transaction an immediate error rather than a silent
 * race — it can't prove the lock is held, but it rules out the "no transaction
 * at all" mistake.
 */
@Component
@RequiredArgsConstructor
public class WaitlistManager {

    private static final int FIRST_POSITION = 1;

    private final RsvpDao rsvpDao;

    @Transactional(propagation = Propagation.MANDATORY)
    public void rebalance(Event lockedEvent) {
        long freeSeats = SeatUtils.availableSeats(lockedEvent.getSeatLimit(),
                rsvpDao.countByEventIdAndStatus(lockedEvent.getId(), RsvpStatus.CONFIRMED));
        List<Rsvp> queue = rsvpDao.findByEventIdAndStatusOrderByPositionAsc(lockedEvent.getId(), RsvpStatus.WAITLISTED);

        // Never negative (a full event promotes nobody), never more than are waiting.
        int promotions = Math.clamp(freeSeats, 0, queue.size());
        queue.subList(0, promotions).forEach(this::confirm);
        renumber(queue.subList(promotions, queue.size()));
    }

    private void confirm(Rsvp rsvp) {
        rsvp.setStatus(RsvpStatus.CONFIRMED);
        rsvp.setPosition(null);
    }

    // Positions are rewritten from 1 rather than shifted by an offset, so the
    // queue comes out contiguous whatever state it went in with.
    private void renumber(List<Rsvp> stillWaiting) {
        for (int index = 0; index < stillWaiting.size(); index++) {
            stillWaiting.get(index).setPosition(FIRST_POSITION + index);
        }
    }
}
