/**
 * Backend message key -> the copy a user sees. The backend never sends prose,
 * only these keys; this is the only place wording lives.
 *
 * Every key the backend can return is listed, taken from the enums:
 * common/type/CommonMessageKeys, event/type/EventMessageKeys,
 * rsvp/type/RsvpMessageKeys, auth/type/AuthMessageKeys. A new backend key
 * needs a line here - until then it shows UNKNOWN_ERROR.
 */
export const MESSAGES = {
  // common/type/CommonMessageKeys.java
  AUTHENTICATION_REQUIRED: "Please sign in to continue.",
  ACCESS_DENIED: "You don't have permission to do that.",
  DATA_INTEGRITY_VIOLATION:
    "That conflicts with existing data. Refresh and try again.",
  INTERNAL_SERVER_ERROR: "Something went wrong on our side. Please try again.",
  MALFORMED_REQUEST_BODY:
    "Something in that request wasn't valid. Please check and try again.",
  INVALID_PARAMETER: "That link or filter isn't valid.",
  PAGE_NUMBER_INVALID: "That page doesn't exist.",
  PAGE_SIZE_INVALID: "That page size isn't allowed.",

  // event/type/EventMessageKeys.java
  EVENT_ERROR_NOT_FOUND: "This event doesn't exist or was removed.",
  EVENT_ERROR_NOT_ORGANIZER: "Only the organiser can do that.",
  EVENT_ERROR_NOT_UPCOMING:
    "This event has already been cancelled or completed.",
  EVENT_ERROR_DATE_MUST_BE_FUTURE: "Pick a date and time in the future.",
  EVENT_ERROR_MEETING_LINK_REQUIRED: "Online events need a meeting link.",
  EVENT_ERROR_LOCATION_REQUIRED: "In-person events need a venue.",
  EVENT_ERROR_SEAT_LIMIT_BELOW_CONFIRMED:
    "The seat limit can't be lower than the number of people already confirmed.",
  EVENT_ERROR_TAG_NOT_FOUND: "One of those tags doesn't exist.",
  EVENT_ERROR_NAME_REQUIRED: "Give your event a name.",
  EVENT_ERROR_NAME_TOO_LONG: "That name is too long.",
  EVENT_ERROR_DESCRIPTION_REQUIRED: "Add a description.",
  EVENT_ERROR_MODE_REQUIRED: "Choose online or in person.",
  EVENT_ERROR_LOCATION_TOO_LONG: "That venue address is too long.",
  EVENT_ERROR_MEETING_LINK_TOO_LONG: "That meeting link is too long.",
  EVENT_ERROR_DATE_REQUIRED: "Pick a date and time.",
  EVENT_ERROR_SEAT_LIMIT_REQUIRED: "Set a seat limit.",
  EVENT_ERROR_SEAT_LIMIT_MIN: "An event needs at least one seat.",
  EVENT_ERROR_TAG_IDS_REQUIRED: "Tags are missing from the request.",

  // rsvp/type/RsvpMessageKeys.java
  RSVP_ERROR_ALREADY_EXISTS: "You've already RSVPed to this event.",
  RSVP_ERROR_NOT_FOUND: "You don't have an active RSVP for this event.",
  RSVP_ERROR_OWN_EVENT: "You can't RSVP to your own event.",
  RSVP_ERROR_EVENT_NOT_UPCOMING:
    "RSVPs are closed - this event was cancelled or has finished.",

  // auth/type/AuthMessageKeys.java
  AUTH_ERROR_EMAIL_ALREADY_REGISTERED:
    "An account with that email already exists.",
  AUTH_ERROR_INVALID_CREDENTIALS: "That email and password don't match.",
  AUTH_ERROR_PASSWORD_TOO_LONG: "That password is too long.",
  AUTH_ERROR_NAME_REQUIRED: "Enter your name.",
  AUTH_ERROR_NAME_TOO_LONG: "That name is too long.",
  AUTH_ERROR_EMAIL_REQUIRED: "Enter your email.",
  AUTH_ERROR_EMAIL_INVALID: "That doesn't look like an email address.",
  AUTH_ERROR_EMAIL_TOO_LONG: "That email is too long.",
  AUTH_ERROR_PASSWORD_REQUIRED: "Enter your password.",
  AUTH_ERROR_PASSWORD_TOO_SHORT: "Use at least 8 characters.",
  AUTH_ERROR_BIO_TOO_LONG: "That bio is too long.",

  // Frontend-only: failures that never reach the backend, or can't be read.
  NETWORK_ERROR: "Can't reach Seatly. Check your connection and try again.",
  SERVICE_UNAVAILABLE:
    "Seatly is temporarily unavailable. Please try again shortly.",
  UNKNOWN_ERROR: "Something went wrong. Please try again.",
} as const;

export type MessageKey = keyof typeof MESSAGES;

export function isMessageKey(value: string): value is MessageKey {
  return Object.hasOwn(MESSAGES, value);
}
