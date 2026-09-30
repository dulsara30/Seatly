import type { Id } from "@/types/entities/primitives";

// Mirrors common/constant/ApiPaths.java; only the Next server calls these directly.
export const BackendPaths = {
  auth: {
    register: "/v1/auth/register",
    login: "/v1/auth/login",
    me: "/v1/auth/me",
  },
  events: {
    list: "/v1/events",
    my: "/v1/events/my",
    cancel: (eventId: Id) => `/v1/events/${eventId}/cancel`,
    detail: (eventId: Id) => `/v1/events/${eventId}`,
    stream: (eventId: Id) => `/v1/events/${eventId}/stream`,
    rsvp: (eventId: Id) => `/v1/events/${eventId}/rsvp`,
    attendees: (eventId: Id) => `/v1/events/${eventId}/attendees`,
    waitlist: (eventId: Id) => `/v1/events/${eventId}/waitlist`,
  },
  rsvps: {
    my: "/v1/rsvps/my",
  },
} as const;

// Must match SeatStreamRegistry.SEAT_UPDATE_EVENT on the backend.
export const SEAT_UPDATE_EVENT = "seat-update";

// The proxy refuses any path without this prefix.
export const BACKEND_PATH_PREFIX = "/v1/";
