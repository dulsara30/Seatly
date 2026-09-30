package com.seatly.backend.event.payload;

/**
 * A Spring ApplicationEvent: "this event's seat counts changed, and here are
 * the new ones." Published by the services; who listens — today the SSE
 * stream — is none of their business.
 *
 * The counts are computed by the publisher, inside the transaction and under
 * the event row's lock, so they're exactly what's about to be committed. A
 * listener only ever sees them after that commit succeeds.
 */
public record SeatCountChangedEvent(SeatCountDto seatCount) {
}
