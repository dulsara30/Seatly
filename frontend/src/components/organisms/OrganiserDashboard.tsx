"use client";

import { useMyEvents } from "@/api/EventApi";
import { Badge } from "@/components/atoms/Badge";
import { LinkButton } from "@/components/atoms/Button";
import { Skeleton } from "@/components/atoms/Skeleton";
import { EmptyState } from "@/components/molecules/EmptyState";
import { ErrorState } from "@/components/molecules/ErrorState";
import { EventListRow } from "@/components/molecules/EventListRow";
import { StatCard } from "@/components/molecules/StatCard";
import { PageHeading } from "@/components/templates/PageHeading";
import { Copy } from "@/constants/copy";
import { Routes } from "@/constants/routes";
import { LIST_SKELETON_COUNT } from "@/constants/ui";
import { EventMode } from "@/types/entities/enums";
import type { EventResponse } from "@/types/responses/EventResponse";
import { dashboardStatsOf } from "@/utils/dashboardStats";
import { formatLongDate, formatTime } from "@/utils/dateTime";
import { takenSeats } from "@/utils/seats";

const STAT_SKELETON_COUNT = 4;

/** 06 Dashboard - four stats and the organiser's events. */
export function OrganiserDashboard() {
  const myEvents = useMyEvents();

  return (
    <div className="flex flex-col gap-8">
      <PageHeading
        title={Copy.dashboard.title}
        subtitle={Copy.dashboard.subtitle}
        action={
          <LinkButton href={Routes.createEvent} variant="accent">
            {Copy.dashboard.createEvent}
          </LinkButton>
        }
      />
      {myEvents.isPending && <DashboardSkeleton />}
      {myEvents.isError && (
        <ErrorState
          title={Copy.dashboard.errorTitle}
          message={myEvents.error.message}
          onRetry={() => void myEvents.refetch()}
        />
      )}
      {myEvents.isSuccess && <DashboardContent events={myEvents.data} />}
    </div>
  );
}

function DashboardContent({ events }: { events: EventResponse[] }) {
  const stats = dashboardStatsOf(events);

  return (
    <>
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard
          value={stats.totalEvents}
          label={Copy.dashboard.totalEvents}
        />
        <StatCard
          value={stats.confirmedAttendees}
          label={Copy.dashboard.confirmedAttendees}
        />
        <StatCard value={stats.openSeats} label={Copy.dashboard.openSeats} />
        <StatCard
          value={stats.upcomingThisWeek}
          label={Copy.dashboard.thisWeek}
          accent
        />
      </div>
      <section className="flex flex-col gap-4">
        <h2 className="text-heading-m text-black">{Copy.dashboard.myEvents}</h2>
        {events.length === 0 ? (
          <EmptyState
            title={Copy.dashboard.emptyTitle}
            body={Copy.dashboard.emptyBody}
            action={
              <LinkButton
                href={Routes.createEvent}
                variant="secondary"
                size="small">
                {Copy.dashboard.createEvent}
              </LinkButton>
            }
          />
        ) : (
          <ul className="rounded-lg border border-gray-200 bg-white px-4">
            {events.map((event) => (
              <EventListRow
                key={event.id}
                date={event.eventDate}
                title={event.name}
                meta={`${formatLongDate(event.eventDate)} · ${formatTime(event.eventDate)} · ${
                  event.mode === EventMode.ONLINE
                    ? Copy.detail.online
                    : event.location
                }`}
                href={Routes.manageEvent(event.id)}
                trailing={
                  <>
                    <Badge variant={event.status} />
                    <span className="text-label-s text-black">
                      {Copy.seats.taken(
                        takenSeats(event.availableSeats, event.seatLimit),
                        event.seatLimit,
                      )}
                    </span>
                    <LinkButton
                      href={Routes.manageEvent(event.id)}
                      variant="secondary"
                      size="small">
                      {Copy.dashboard.manage}
                    </LinkButton>
                  </>
                }
              />
            ))}
          </ul>
        )}
      </section>
    </>
  );
}

function DashboardSkeleton() {
  return (
    <div aria-label={Copy.common.loading} className="flex flex-col gap-6">
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {Array.from({ length: STAT_SKELETON_COUNT }, (_, index) => (
          <Skeleton key={index} className="h-24 w-full" />
        ))}
      </div>
      {Array.from({ length: LIST_SKELETON_COUNT }, (_, index) => (
        <Skeleton key={index} className="h-16 w-full" />
      ))}
    </div>
  );
}
