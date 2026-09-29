import type { Id } from "@/types/entities/primitives";
import type { EventListParams } from "@/types/requests/EventRequests";

/**
 * Every TanStack Query cache key. Keys are hierarchical on purpose:
 * invalidating QueryKeys.events.all refreshes every list, every detail and
 * every attendee/waitlist view under it — which is exactly what an RSVP
 * change needs, since it moves seat counts everywhere.
 */
export const QueryKeys = {
  auth: {
    session: ["auth", "session"] as const,
  },
  events: {
    all: ["events"] as const,
    lists: ["events", "list"] as const,
    list: (params: Omit<EventListParams, "page">) => ["events", "list", params] as const,
    detail: (eventId: Id) => ["events", "detail", eventId] as const,
    attendees: (eventId: Id) => ["events", "detail", eventId, "attendees"] as const,
    waitlist: (eventId: Id) => ["events", "detail", eventId, "waitlist"] as const,
  },
  rsvps: {
    my: ["rsvps", "my"] as const,
  },
} as const;
