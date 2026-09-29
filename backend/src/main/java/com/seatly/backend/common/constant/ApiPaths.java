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
    public static final String EVENTS_SINGLE_SEGMENT = EVENTS + "/*";

    private ApiPaths() {
    }
}
