package com.seatly.backend.user.model;

public final class UserFieldLimits {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int EMAIL_MAX_LENGTH = 255;
    public static final int BIO_MAX_LENGTH = 500;

    public static final int PASSWORD_MIN_LENGTH = 8;

    // BCrypt reads only 72 bytes; @Size counts chars, so the service checks the byte length.
    public static final int PASSWORD_MAX_BYTES = 72;

    private UserFieldLimits() {
    }
}
