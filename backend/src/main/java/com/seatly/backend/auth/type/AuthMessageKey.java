package com.seatly.backend.auth.type;

import com.seatly.backend.common.type.MessageKey;

public enum AuthMessageKey implements MessageKey {

    EMAIL_ALREADY_REGISTERED(AuthMessageKeys.EMAIL_ALREADY_REGISTERED),
    INVALID_CREDENTIALS(AuthMessageKeys.INVALID_CREDENTIALS),
    PASSWORD_TOO_LONG(AuthMessageKeys.PASSWORD_TOO_LONG),

    NAME_REQUIRED(AuthMessageKeys.NAME_REQUIRED),
    NAME_TOO_LONG(AuthMessageKeys.NAME_TOO_LONG),
    EMAIL_REQUIRED(AuthMessageKeys.EMAIL_REQUIRED),
    EMAIL_INVALID(AuthMessageKeys.EMAIL_INVALID),
    EMAIL_TOO_LONG(AuthMessageKeys.EMAIL_TOO_LONG),
    PASSWORD_REQUIRED(AuthMessageKeys.PASSWORD_REQUIRED),
    PASSWORD_TOO_SHORT(AuthMessageKeys.PASSWORD_TOO_SHORT),
    BIO_TOO_LONG(AuthMessageKeys.BIO_TOO_LONG);

    private final String key;

    AuthMessageKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
