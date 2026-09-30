package com.seatly.backend.common.constant;

public final class ApiPaths {

    public static final String V1 = "/v1";

    public static final String AUTH = V1 + "/auth";
    public static final String AUTH_REGISTER = AUTH + "/register";
    public static final String AUTH_LOGIN = AUTH + "/login";
    public static final String AUTH_ME = AUTH + "/me";

    public static final String EVENTS = V1 + "/events";
    // Also matches /events/my, so any private GET /events/x needs its own rule above this one.
    public static final String EVENTS_SINGLE_SEGMENT = EVENTS + "/*";

    public static final String EVENTS_MY = EVENTS + "/my";
    public static final String EVENT_CANCEL = EVENTS + "/{eventId}/cancel";

    public static final String EVENT_STREAM = EVENTS + "/{eventId}/stream";

    public static final String EVENT_RSVP = EVENTS + "/{eventId}/rsvp";
    public static final String EVENT_ATTENDEES = EVENTS + "/{eventId}/attendees";
    public static final String EVENT_WAITLIST = EVENTS + "/{eventId}/waitlist";

    public static final String RSVPS = V1 + "/rsvps";
    public static final String MY_RSVPS = RSVPS + "/my";

    private ApiPaths() {
    }
}
