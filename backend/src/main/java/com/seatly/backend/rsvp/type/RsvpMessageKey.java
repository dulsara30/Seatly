package com.seatly.backend.rsvp.type;

import com.seatly.backend.common.type.MessageKey;

public enum RsvpMessageKey implements MessageKey {

    ALREADY_EXISTS(RsvpMessageKeys.ALREADY_EXISTS),
    NOT_FOUND(RsvpMessageKeys.NOT_FOUND),
    OWN_EVENT(RsvpMessageKeys.OWN_EVENT),
    EVENT_NOT_UPCOMING(RsvpMessageKeys.EVENT_NOT_UPCOMING);

    private final String key;

    RsvpMessageKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
