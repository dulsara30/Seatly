import type { Id } from "@/types/entities/primitives";

/**
 * The Spring backend's own paths — mirrors common/constant/ApiPaths.java.
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
    detail: (eventId: Id) => `/v1/events/${eventId}`,
    rsvp: (eventId: Id) => `/v1/events/${eventId}/rsvp`,
    attendees: (eventId: Id) => `/v1/events/${eventId}/attendees`,
    waitlist: (eventId: Id) => `/v1/events/${eventId}/waitlist`,
  },
  rsvps: {
    my: "/v1/rsvps/my",
  },
} as const;

/** Every backend path starts with this; the proxy refuses anything else. */
export const BACKEND_PATH_PREFIX = "/v1/";
