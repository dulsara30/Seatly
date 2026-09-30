import type { Id } from "@/types/entities/primitives";

/** Every page route in the app. */
export const Routes = {
  home: "/",
  signIn: "/sign-in",
  signUp: "/sign-up",
  eventDetail: (eventId: Id) => `/events/${eventId}`,
  myRsvps: "/my-rsvps",
  dashboard: "/dashboard",
  createEvent: "/dashboard/events/new",
  manageEvent: (eventId: Id) => `/dashboard/events/${eventId}`,
} as const;

/**
 * The other spellings people type for sign in / sign up. next.config.ts
 * redirects each to the canonical route, so a guess lands on the page
 * instead of a 404.
 */
export const RouteAliases = {
  signIn: ["/login", "/log-in", "/signin"],
  signUp: ["/register", "/signup"],
} as const;

/**
 * Query param sign-in reads to send you back where you were headed. Always a
 * same-site path; whatever reads it must reject anything that isn't one
 * ("//evil.example" included), or sign-in becomes an open redirect.
 */
export const RETURN_TO_PARAM = "returnTo";

/** "/sign-in?returnTo=/events/4" - sign in, then land back where you were. */
export const withReturnTo = (route: string, returnTo: string | undefined) =>
  returnTo
    ? `${route}?${new URLSearchParams({ [RETURN_TO_PARAM]: returnTo })}`
    : route;
