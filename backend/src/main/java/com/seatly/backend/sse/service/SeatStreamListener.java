package com.seatly.backend.sse.service;

import com.seatly.backend.event.payload.SeatCountChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The bridge from "seats changed" to "tell the browsers". The services
 * publish; this is the only subscriber, and the only code in the domain path
 * that knows SSE exists.
 *
 * AFTER_COMMIT, not a plain @EventListener. A plain listener runs the moment
 * publishEvent is called — still inside the RSVP transaction. If that
 * transaction then rolled back (a constraint violation, a failure after the
 * publish), every browser would already have been told a seat was taken that
 * never was. AFTER_COMMIT holds the event until the database has durably
 * accepted the change, and drops it if the transaction rolls back.
 *
 * It also means the push can't outrun the data: a browser reacting to the
 * update by fetching the event sees the committed state, not the old one.
 *
 * This is also the one class to change for multi-instance fan-out: publish
 * the event to Redis here, and broadcast locally from a Redis subscriber.
 */
@Component
@RequiredArgsConstructor
public class SeatStreamListener {

    private final SeatStreamRegistry registry;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSeatCountChanged(SeatCountChangedEvent event) {
        registry.broadcast(event.seatCount());
    }
}
