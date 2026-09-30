package com.seatly.backend.event.type;

import com.seatly.backend.common.type.MessageKey;

public enum EventMessageKey implements MessageKey {

    NOT_FOUND(EventMessageKeys.NOT_FOUND),
    NOT_ORGANIZER(EventMessageKeys.NOT_ORGANIZER),
    NOT_UPCOMING(EventMessageKeys.NOT_UPCOMING),
    DATE_MUST_BE_FUTURE(EventMessageKeys.DATE_MUST_BE_FUTURE),
    MEETING_LINK_REQUIRED(EventMessageKeys.MEETING_LINK_REQUIRED),
    LOCATION_REQUIRED(EventMessageKeys.LOCATION_REQUIRED),
    SEAT_LIMIT_BELOW_CONFIRMED(EventMessageKeys.SEAT_LIMIT_BELOW_CONFIRMED),
    TAG_NOT_FOUND(EventMessageKeys.TAG_NOT_FOUND),

    NAME_REQUIRED(EventMessageKeys.NAME_REQUIRED),
    NAME_TOO_LONG(EventMessageKeys.NAME_TOO_LONG),
    DESCRIPTION_REQUIRED(EventMessageKeys.DESCRIPTION_REQUIRED),
    MODE_REQUIRED(EventMessageKeys.MODE_REQUIRED),
    LOCATION_TOO_LONG(EventMessageKeys.LOCATION_TOO_LONG),
    MEETING_LINK_TOO_LONG(EventMessageKeys.MEETING_LINK_TOO_LONG),
    DATE_REQUIRED(EventMessageKeys.DATE_REQUIRED),
    SEAT_LIMIT_REQUIRED(EventMessageKeys.SEAT_LIMIT_REQUIRED),
    SEAT_LIMIT_MIN(EventMessageKeys.SEAT_LIMIT_MIN),
    TAG_IDS_REQUIRED(EventMessageKeys.TAG_IDS_REQUIRED);

    private final String key;

    EventMessageKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
