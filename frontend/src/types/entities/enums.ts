/**
 * Mirrors of the backend enums. Each is a const object AND a type of the same
 * name, so code writes EventMode.ONLINE rather than the string "ONLINE", and
 * the type only admits the values the backend can actually send.
 */

// event/type/EventMode.java
export const EventMode = {
  ONLINE: "ONLINE",
  PHYSICAL: "PHYSICAL",
} as const;
export type EventMode = (typeof EventMode)[keyof typeof EventMode];

// event/type/EventStatus.java
export const EventStatus = {
  UPCOMING: "UPCOMING",
  CANCELLED: "CANCELLED",
  COMPLETED: "COMPLETED",
} as const;
export type EventStatus = (typeof EventStatus)[keyof typeof EventStatus];

// rsvp/type/RsvpStatus.java
export const RsvpStatus = {
  CONFIRMED: "CONFIRMED",
  WAITLISTED: "WAITLISTED",
  CANCELLED: "CANCELLED",
} as const;
export type RsvpStatus = (typeof RsvpStatus)[keyof typeof RsvpStatus];
