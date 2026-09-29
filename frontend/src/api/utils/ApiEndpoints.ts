import { BackendPaths } from "@/api/utils/BackendPaths";
import type { Id } from "@/types/entities/primitives";

/**
 * Every URL the browser calls, relative to axiosInstance's baseURL (/api).
 *
 * Two kinds:
 *  - Next's own auth routes (login, logout, session). They exist because the
 *    token lives in an httpOnly cookie that only the Next server can set or read.
 *  - Everything else, reached through the proxy route, which attaches the
 *    token from that cookie and forwards to Spring.
 */
/** axiosInstance's baseURL — every browser call is same-origin, into Next. */
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
    detail: (eventId: Id) => viaProxy(BackendPaths.events.detail(eventId)),
    rsvp: (eventId: Id) => viaProxy(BackendPaths.events.rsvp(eventId)),
    attendees: (eventId: Id) => viaProxy(BackendPaths.events.attendees(eventId)),
    waitlist: (eventId: Id) => viaProxy(BackendPaths.events.waitlist(eventId)),
  },
  rsvps: {
    my: viaProxy(BackendPaths.rsvps.my),
  },
} as const;
