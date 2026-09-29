package com.seatly.backend.user.model;

/**
 * Column limits from V1__init.sql, shared by the entity mapping and the
 * request DTOs — same pattern as EventFieldLimits.
 */
public final class UserFieldLimits {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int EMAIL_MAX_LENGTH = 255;
    public static final int BIO_MAX_LENGTH = 500;

    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * BCrypt only reads the first 72 BYTES of a password, and Spring's
     * encoder refuses longer input outright. @Size counts characters, not
     * bytes, so it can't enforce this for non-ASCII passwords — the service
     * checks the byte length itself.
     */
    public static final int PASSWORD_MAX_BYTES = 72;

    private UserFieldLimits() {
    }
}
