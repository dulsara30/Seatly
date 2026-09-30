import type { Id } from "@/types/entities/primitives";
import type { EventListParams } from "@/types/requests/EventRequests";

// Hierarchical so invalidating a prefix (events.all) refreshes everything under it.
export const QueryKeys = {
  auth: {
    session: ["auth", "session"] as const,
  },
  events: {
    all: ["events"] as const,
    lists: ["events", "list"] as const,
    list: (params: Omit<EventListParams, "page">) =>
      ["events", "list", params] as const,
    mine: ["events", "mine"] as const,
    detail: (eventId: Id) => ["events", "detail", eventId] as const,
    attendees: (eventId: Id) =>
      ["events", "detail", eventId, "attendees"] as const,
    waitlist: (eventId: Id) =>
      ["events", "detail", eventId, "waitlist"] as const,
  },
  rsvps: {
    my: ["rsvps", "my"] as const,
  },
} as const;
