import { UPCOMING_WINDOW_DAYS } from "@/constants/ui";
import { EventStatus } from "@/types/entities/enums";
import type { EventResponse } from "@/types/responses/EventResponse";
import { isWithinDays } from "@/utils/dateTime";
import { takenSeats } from "@/utils/seats";

export interface DashboardStats {
  totalEvents: number;
  confirmedAttendees: number;
  openSeats: number;
  upcomingThisWeek: number;
}

/**
 * The four dashboard numbers, derived from the organiser's own events.
 *
 * The design's fourth card is "People on waitlists", but no event response
 * carries a waitlist count - it would take one request per event to find
 * out. "Open seats" is shown instead, until the backend adds the count.
 */
export function dashboardStatsOf(
  events: EventResponse[],
  now: Date = new Date(),
): DashboardStats {
  const upcoming = events.filter(
    (event) => event.status === EventStatus.UPCOMING,
  );
  return {
    totalEvents: events.length,
    confirmedAttendees: events.reduce(
      (sum, event) => sum + takenSeats(event.availableSeats, event.seatLimit),
      0,
    ),
    openSeats: upcoming.reduce(
      (sum, event) => sum + Math.max(event.availableSeats, 0),
      0,
    ),
    upcomingThisWeek: upcoming.filter((event) =>
      isWithinDays(event.eventDate, UPCOMING_WINDOW_DAYS, now),
    ).length,
  };
}
