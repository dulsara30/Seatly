package com.seatly.backend.sse.service;

import com.seatly.backend.event.payload.SeatCountChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SeatStreamListener {

    private final SeatStreamRegistry registry;

    // AFTER_COMMIT, not @EventListener: a rollback would push a seat change that never happened.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSeatCountChanged(SeatCountChangedEvent event) {
        registry.broadcast(event.seatCount());
    }
}
