package com.seatly.backend.event.model;

/**
 * Column limits from V1__init.sql, shared by the entity's @Column mappings and
 * the request DTOs' Bean Validation, so the database and the API never
 * disagree about how long a field may be.
 */
public final class EventFieldLimits {

    public static final int NAME_MAX_LENGTH = 255;
    public static final int LOCATION_MAX_LENGTH = 255;
    public static final int MEETING_LINK_MAX_LENGTH = 500;
    public static final int SEAT_LIMIT_MIN = 1;

    private EventFieldLimits() {
    }
}
