package com.seatly.backend.rsvp.type;

/** Single source of every RSVP message key string — same pattern as EventMessageKeys. */
public interface RsvpMessageKeys {

    String ALREADY_EXISTS = "RSVP_ERROR_ALREADY_EXISTS";
    String NOT_FOUND = "RSVP_ERROR_NOT_FOUND";
    String OWN_EVENT = "RSVP_ERROR_OWN_EVENT";
    String EVENT_NOT_UPCOMING = "RSVP_ERROR_EVENT_NOT_UPCOMING";
}
