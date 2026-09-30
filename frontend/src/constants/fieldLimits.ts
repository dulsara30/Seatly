/**
 * Mirrors of the backend's field limits, so the forms reject exactly what
 * Spring would reject - before the request is sent.
 */

// event/model/EventFieldLimits.java
export const EventFieldLimits = {
  NAME_MAX_LENGTH: 255,
  LOCATION_MAX_LENGTH: 255,
  MEETING_LINK_MAX_LENGTH: 500,
  SEAT_LIMIT_MIN: 1,
} as const;

// user/model/UserFieldLimits.java
export const UserFieldLimits = {
  NAME_MAX_LENGTH: 100,
  EMAIL_MAX_LENGTH: 255,
  BIO_MAX_LENGTH: 500,
  PASSWORD_MIN_LENGTH: 8,
  /** BCrypt reads only the first 72 BYTES. Measured in UTF-8 bytes, not characters. */
  PASSWORD_MAX_BYTES: 72,
} as const;
