package com.seatly.backend.common.util;

public final class SeatUtils {

    private SeatUtils() {
    }

    public static long availableSeats(int seatLimit, long confirmedCount) {
        return seatLimit - confirmedCount;
    }
}
