import type { EventStatus, RsvpStatus } from "@/types/entities/enums";
import type { Id, LocalDateTimeString } from "@/types/entities/primitives";

// rsvp/payload/RsvpResponseDto.java; position is set only while WAITLISTED (1 = next).
export interface RsvpResponse {
  eventId: Id;
  status: RsvpStatus;
  position: number | null;
}

// rsvp/payload/MyRsvpResponseDto.java; stays active if the event is cancelled - check eventStatus.
export interface MyRsvpResponse {
  eventId: Id;
  eventName: string;
  eventDate: LocalDateTimeString;
  eventStatus: EventStatus;
  status: RsvpStatus;
  position: number | null;
}

// rsvp/payload/AttendeeResponseDto.java - organiser-only, so it carries email.
export interface AttendeeResponse {
  userId: Id;
  name: string;
  email: string;
  rsvpAt: LocalDateTimeString;
}

// rsvp/payload/AttendeeListResponseDto.java
export interface AttendeeListResponse {
  items: AttendeeResponse[];
  confirmedCount: number;
  seatLimit: number;
}

// rsvp/payload/WaitlistEntryResponseDto.java - organiser-only.
export interface WaitlistEntryResponse {
  userId: Id;
  name: string;
  email: string;
  position: number;
  rsvpAt: LocalDateTimeString;
}
