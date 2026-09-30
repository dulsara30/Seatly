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
