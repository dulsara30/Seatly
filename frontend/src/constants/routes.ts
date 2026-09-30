import type { Id } from "@/types/entities/primitives";

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

// next.config.ts redirects each alias to the canonical route.
export const RouteAliases = {
  signIn: ["/login", "/log-in", "/signin"],
  signUp: ["/register", "/signup"],
} as const;

// Readers must accept only same-site paths, or sign-in becomes an open redirect.
export const RETURN_TO_PARAM = "returnTo";

export const withReturnTo = (route: string, returnTo: string | undefined) =>
  returnTo
    ? `${route}?${new URLSearchParams({ [RETURN_TO_PARAM]: returnTo })}`
    : route;
