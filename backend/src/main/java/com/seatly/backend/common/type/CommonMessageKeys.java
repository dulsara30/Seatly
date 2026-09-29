package com.seatly.backend.common.type;

/**
 * The single source of every common message key string — same pattern as
 * EventMessageKeys, so annotations and the enum share one spelling.
 */
public interface CommonMessageKeys {

    String VALIDATION_FAILED = "VALIDATION_FAILED";
    String ACCESS_DENIED = "ACCESS_DENIED";
    String DATA_INTEGRITY_VIOLATION = "DATA_INTEGRITY_VIOLATION";
    String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    String MALFORMED_REQUEST_BODY = "MALFORMED_REQUEST_BODY";
    String INVALID_PARAMETER = "INVALID_PARAMETER";
    String PAGE_NUMBER_INVALID = "PAGE_NUMBER_INVALID";
    String PAGE_SIZE_INVALID = "PAGE_SIZE_INVALID";
}
