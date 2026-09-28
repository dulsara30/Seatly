package com.seatly.backend.common.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResponseStatusType {

    SUCCESSFUL("successful"),
    UNSUCCESSFUL("unsuccessful");

    private final String value;

    ResponseStatusType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
