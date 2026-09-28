package com.seatly.backend.common.type;

/**
 * Message keys for errors that originate outside any single module —
 * framework-level failures GlobalExceptionHandler catches directly, rather
 * than a ModuleException a service threw on purpose.
 */
public enum CommonMessageKey implements MessageKey {

    ACCESS_DENIED,
    DATA_INTEGRITY_VIOLATION,
    INTERNAL_SERVER_ERROR;

    @Override
    public String getKey() {
        return name();
    }
}
