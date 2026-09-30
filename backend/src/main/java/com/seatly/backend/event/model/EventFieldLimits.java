package com.seatly.backend.event.model;

public final class EventFieldLimits {

    public static final int NAME_MAX_LENGTH = 255;
    public static final int LOCATION_MAX_LENGTH = 255;
    public static final int MEETING_LINK_MAX_LENGTH = 500;
    public static final int SEAT_LIMIT_MIN = 1;

    private EventFieldLimits() {
    }
}
