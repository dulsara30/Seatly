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

// No response carries a waitlist count, so the fourth card shows open seats instead.
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
