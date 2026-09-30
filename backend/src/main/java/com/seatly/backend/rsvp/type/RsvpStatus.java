package com.seatly.backend.rsvp.type;

import java.util.Set;

public enum RsvpStatus {

    CONFIRMED,
    WAITLISTED,
    CANCELLED;

    public static final Set<RsvpStatus> ACTIVE = Set.of(CONFIRMED, WAITLISTED);

    public boolean isActive() {
        return ACTIVE.contains(this);
    }
}
