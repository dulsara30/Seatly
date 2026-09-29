package com.seatly.backend.common.constant;

/**
 * Every API path, defined once. Controllers map to these and SecurityConfig
 * authorises against the same constants, so a renamed path can't silently
 * fall out of its access rule.
 */
public final class ApiPaths {

    public static final String V1 = "/v1";

    public static final String AUTH = V1 + "/auth";
    public static final String AUTH_REGISTER = AUTH + "/register";
    public static final String AUTH_LOGIN = AUTH + "/login";
    public static final String AUTH_ME = AUTH + "/me";

    public static final String EVENTS = V1 + "/events";
    // One path segment below /events — matches /events/42, not /events/42/rsvp.
    // Careful: a future GET /events/my would also match, and be public, unless
    // it gets its own rule listed above this one in SecurityConfig.
    public static final String EVENTS_SINGLE_SEGMENT = EVENTS + "/*";

    public static final String EVENT_RSVP = EVENTS + "/{eventId}/rsvp";
    public static final String EVENT_ATTENDEES = EVENTS + "/{eventId}/attendees";
    public static final String EVENT_WAITLIST = EVENTS + "/{eventId}/waitlist";

    public static final String RSVPS = V1 + "/rsvps";
    public static final String MY_RSVPS = RSVPS + "/my";

    private ApiPaths() {
    }
}
