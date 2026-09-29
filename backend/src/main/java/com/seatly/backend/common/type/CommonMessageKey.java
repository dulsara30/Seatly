package com.seatly.backend.common.type;

/**
 * Message keys for errors that originate outside any single module —
 * framework-level failures GlobalExceptionHandler catches directly, rather
 * than a ModuleException a service threw on purpose.
 */
public enum CommonMessageKey implements MessageKey {

    ACCESS_DENIED(CommonMessageKeys.ACCESS_DENIED),
    DATA_INTEGRITY_VIOLATION(CommonMessageKeys.DATA_INTEGRITY_VIOLATION),
    INTERNAL_SERVER_ERROR(CommonMessageKeys.INTERNAL_SERVER_ERROR),
    MALFORMED_REQUEST_BODY(CommonMessageKeys.MALFORMED_REQUEST_BODY),
    INVALID_PARAMETER(CommonMessageKeys.INVALID_PARAMETER),
    PAGE_NUMBER_INVALID(CommonMessageKeys.PAGE_NUMBER_INVALID),
    PAGE_SIZE_INVALID(CommonMessageKeys.PAGE_SIZE_INVALID);

    private final String key;

    CommonMessageKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
