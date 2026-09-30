import { BackendPaths } from "@/api/utils/BackendPaths";
import type { Id } from "@/types/entities/primitives";

export const API_BASE_PATH = "/api";

const PROXY = "/proxy";

const viaProxy = (backendPath: string) => `${PROXY}${backendPath}`;

export const ApiEndpoints = {
  auth: {
    login: "/auth/login",
    logout: "/auth/logout",
    session: "/auth/session",
    register: viaProxy(BackendPaths.auth.register),
  },
  events: {
    list: viaProxy(BackendPaths.events.list),
    my: viaProxy(BackendPaths.events.my),
    cancel: (eventId: Id) => viaProxy(BackendPaths.events.cancel(eventId)),
    detail: (eventId: Id) => viaProxy(BackendPaths.events.detail(eventId)),
    rsvp: (eventId: Id) => viaProxy(BackendPaths.events.rsvp(eventId)),
    attendees: (eventId: Id) =>
      viaProxy(BackendPaths.events.attendees(eventId)),
    waitlist: (eventId: Id) => viaProxy(BackendPaths.events.waitlist(eventId)),
  },
  rsvps: {
    my: viaProxy(BackendPaths.rsvps.my),
  },
} as const;

// EventSource bypasses axios, so this URL must include the /api base path itself.
export const eventSeatStreamUrl = (eventId: Id) =>
  `${API_BASE_PATH}${viaProxy(BackendPaths.events.stream(eventId))}`;
