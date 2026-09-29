package com.seatly.backend.rsvp.type;

import java.util.Set;

public enum RsvpStatus {

    CONFIRMED,
    WAITLISTED,
    CANCELLED;

    /** Statuses that hold a place — a seat or a spot in the queue. */
    public static final Set<RsvpStatus> ACTIVE = Set.of(CONFIRMED, WAITLISTED);

    public boolean isActive() {
        return ACTIVE.contains(this);
    }
}
