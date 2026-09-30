import type { Id } from "@/types/entities/primitives";

/**
 * The Spring backend's own paths - mirrors common/constant/ApiPaths.java.
 * The browser never calls these directly: they are reached through the Next
 * proxy (see ApiEndpoints), and the Next server uses them for login and the
 * session check.
 */
export const BackendPaths = {
  auth: {
    register: "/v1/auth/register",
    login: "/v1/auth/login",
    me: "/v1/auth/me",
  },
  events: {
    list: "/v1/events",
    // results = the caller's own events, every status (not paged)
    my: "/v1/events/my",
    cancel: (eventId: Id) => `/v1/events/${eventId}/cancel`,
    detail: (eventId: Id) => `/v1/events/${eventId}`,
    // Server-Sent Events: text/event-stream of SEAT_UPDATE_EVENT messages.
    stream: (eventId: Id) => `/v1/events/${eventId}/stream`,
    rsvp: (eventId: Id) => `/v1/events/${eventId}/rsvp`,
    attendees: (eventId: Id) => `/v1/events/${eventId}/attendees`,
    waitlist: (eventId: Id) => `/v1/events/${eventId}/waitlist`,
  },
  rsvps: {
    my: "/v1/rsvps/my",
  },
} as const;

/** The SSE event name the stream uses — SeatStreamRegistry.SEAT_UPDATE_EVENT on the backend. */
export const SEAT_UPDATE_EVENT = "seat-update";

/** Every backend path starts with this; the proxy refuses anything else. */
export const BACKEND_PATH_PREFIX = "/v1/";
