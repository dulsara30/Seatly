import type { Id } from "@/types/entities/primitives";

/** Every page route in the app. Pages don't exist yet — these are the planned URLs. */
export const Routes = {
  home: "/",
  login: "/login",
  register: "/register",
  eventDetail: (eventId: Id) => `/events/${eventId}`,
  myRsvps: "/my-rsvps",
  dashboard: "/dashboard",
  createEvent: "/dashboard/events/new",
  manageEvent: (eventId: Id) => `/dashboard/events/${eventId}`,
} as const;

/**
 * Query param login reads to send you back where you were headed. Always a
 * same-site path; whatever reads it must reject anything that isn't one
 * ("//evil.example" included), or login becomes an open redirect.
 */
export const RETURN_TO_PARAM = "returnTo";
