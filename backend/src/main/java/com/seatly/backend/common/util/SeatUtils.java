package com.seatly.backend.common.util;

/**
 * The one seat formula. Event responses report it, RSVP creation decides
 * CONFIRMED vs WAITLISTED with it, and waitlist promotion fills exactly this
 * many seats — so the three can never disagree about what "full" means.
 */
public final class SeatUtils {

    private SeatUtils() {
    }

    public static long availableSeats(int seatLimit, long confirmedCount) {
        return seatLimit - confirmedCount;
    }
}
