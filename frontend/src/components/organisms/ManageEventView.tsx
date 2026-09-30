"use client";

import Link from "next/link";
import { useState } from "react";
import { useEvent, useUpdateEvent } from "@/api/EventApi";
import { useAttendees, useWaitlist } from "@/api/RsvpApi";
import { Badge } from "@/components/atoms/Badge";
import { Button } from "@/components/atoms/Button";
import { Icon } from "@/components/atoms/Icon";
import { SeatBar } from "@/components/atoms/SeatBar";
import { Skeleton } from "@/components/atoms/Skeleton";
import { Stepper } from "@/components/atoms/Stepper";
import { AttendeeRow } from "@/components/molecules/AttendeeRow";
import { EmptyState } from "@/components/molecules/EmptyState";
import { ErrorState } from "@/components/molecules/ErrorState";
import { Tabs } from "@/components/molecules/Tabs";
import { WaitlistRow } from "@/components/molecules/WaitlistRow";
import { CancelEventDialog } from "@/components/organisms/CancelEventDialog";
import { Copy } from "@/constants/copy";
import { EventFieldLimits } from "@/constants/fieldLimits";
import { Routes } from "@/constants/routes";
import { LIST_SKELETON_COUNT } from "@/constants/ui";
import { ToastTone, useToastStore } from "@/store/toastStore";
import { EventMode, EventStatus } from "@/types/entities/enums";
import type { Id } from "@/types/entities/primitives";
import type { EventDetailResponse } from "@/types/responses/EventResponse";
import { formatLongDate, formatTime } from "@/utils/dateTime";
import { takenSeats } from "@/utils/seats";

const AttendanceTab = { CONFIRMED: "CONFIRMED", WAITLIST: "WAITLIST" } as const;
type AttendanceTab = (typeof AttendanceTab)[keyof typeof AttendanceTab];

export function ManageEventView({ eventId }: { eventId: Id }) {
  const event = useEvent(eventId);

  if (event.isPending) {
    return <ManageSkeleton />;
  }
  if (event.isError) {
    return (
      <ErrorState
        title={Copy.manage.errorTitle}
        message={event.error.message}
        onRetry={() => void event.refetch()}
      />
    );
  }

  const isUpcoming = event.data.status === EventStatus.UPCOMING;
  return (
    <div className="flex flex-col gap-8">
      <Link
        href={Routes.dashboard}
        className="inline-flex w-fit items-center gap-1.5 text-label-s text-gray-500 hover:text-black">
        <Icon name="arrowLeft" />
        {Copy.manage.backToDashboard}
      </Link>
      <EventHeader event={event.data} />
      {isUpcoming ? (
        // Keyed on the saved limit, so the stepper resets whenever the server's value changes.
        <SeatLimitEditor key={event.data.seatLimit} event={event.data} />
      ) : (
        <p className="rounded-md bg-gray-100 px-4 py-3 text-body-s text-gray-500">
          {Copy.manage.notUpcoming}
        </p>
      )}
      <Attendance eventId={eventId} />
      {isUpcoming && <DangerZone event={event.data} />}
    </div>
  );
}

function EventHeader({ event }: { event: EventDetailResponse }) {
  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-wrap gap-2">
        <Badge variant={event.status} />
        <Badge variant={event.mode} />
      </div>
      <h1 className="text-heading-xl text-black">{event.name}</h1>
      <p className="text-body-m text-gray-500">
        {formatLongDate(event.eventDate)} · {formatTime(event.eventDate)} ·{" "}
        {event.mode === EventMode.ONLINE ? Copy.detail.online : event.location}
      </p>
    </div>
  );
}

// Can't go below confirmed seats: that would un-confirm someone, which the backend refuses.
function SeatLimitEditor({ event }: { event: EventDetailResponse }) {
  const [seatLimit, setSeatLimit] = useState(event.seatLimit);
  const updateEvent = useUpdateEvent();
  const showToast = useToastStore((state) => state.show);
  const confirmed = takenSeats(event.availableSeats, event.seatLimit);

  const apply = () =>
    updateEvent.mutate(
      { eventId: event.id, request: { seatLimit } },
      {
        onSuccess: (updated) =>
          showToast(
            ToastTone.SUCCESS,
            Copy.toast.seatLimitUpdated(updated.seatLimit),
          ),
        onError: (error) => showToast(ToastTone.ERROR, error.message),
      },
    );

  return (
    <section className="flex flex-col gap-4 rounded-lg border border-gray-200 bg-white p-5">
      <SeatBar
        availableSeats={event.availableSeats}
        seatLimit={event.seatLimit}
      />
      <div className="flex flex-wrap items-end gap-3">
        <Stepper
          label={Copy.manage.seatLimit}
          value={seatLimit}
          min={Math.max(EventFieldLimits.SEAT_LIMIT_MIN, confirmed)}
          onChange={setSeatLimit}
          decreaseLabel={Copy.eventForm.decrease}
          increaseLabel={Copy.eventForm.increase}
        />
        <Button
          variant="primary"
          disabled={seatLimit === event.seatLimit || updateEvent.isPending}
          onClick={apply}>
          {Copy.manage.apply}
        </Button>
      </div>
    </section>
  );
}

function Attendance({ eventId }: { eventId: Id }) {
  const [tab, setTab] = useState<AttendanceTab>(AttendanceTab.CONFIRMED);
  const attendees = useAttendees(eventId);
  const waitlist = useWaitlist(eventId);

  const tabs = [
    {
      id: AttendanceTab.CONFIRMED,
      label: Copy.manage.confirmedTab(attendees.data?.confirmedCount),
    },
    {
      id: AttendanceTab.WAITLIST,
      label: Copy.manage.waitlistTab(waitlist.data?.length),
    },
  ] as const;

  const query = tab === AttendanceTab.CONFIRMED ? attendees : waitlist;

  return (
    <section className="flex flex-col gap-4">
      <Tabs
        label={Copy.manage.tabsLabel}
        tabs={tabs}
        active={tab}
        onChange={setTab}
      />
      {tab === AttendanceTab.WAITLIST &&
        waitlist.isSuccess &&
        waitlist.data.length > 0 && (
          <p className="rounded-md border border-yellow-500 bg-yellow-50 px-4 py-3 text-body-s text-yellow-700">
            {Copy.manage.promoteBanner}
          </p>
        )}
      {query.isPending && <ListSkeleton />}
      {query.isError && (
        <ErrorState
          title={Copy.manage.errorTitle}
          message={query.error.message}
          onRetry={() => void query.refetch()}
        />
      )}
      {tab === AttendanceTab.CONFIRMED &&
        attendees.isSuccess &&
        (attendees.data.items.length === 0 ? (
          <EmptyState
            title={Copy.manage.noAttendeesTitle}
            body={Copy.manage.noAttendeesBody}
          />
        ) : (
          <ul className="rounded-lg border border-gray-200 bg-white px-4">
            {attendees.data.items.map((attendee) => (
              <AttendeeRow key={attendee.userId} {...attendee} />
            ))}
          </ul>
        ))}
      {tab === AttendanceTab.WAITLIST &&
        waitlist.isSuccess &&
        (waitlist.data.length === 0 ? (
          <EmptyState
            title={Copy.manage.noWaitlistTitle}
            body={Copy.manage.noWaitlistBody}
          />
        ) : (
          <ol className="rounded-lg border border-gray-200 bg-white px-4">
            {waitlist.data.map((entry) => (
              <WaitlistRow key={entry.userId} {...entry} />
            ))}
          </ol>
        ))}
    </section>
  );
}

function DangerZone({ event }: { event: EventDetailResponse }) {
  const [confirming, setConfirming] = useState(false);

  return (
    <section className="flex flex-col gap-3 rounded-lg border border-red-300 bg-red-50 p-5 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex flex-col gap-1">
        <h2 className="text-heading-s text-red-600">
          {Copy.manage.dangerTitle}
        </h2>
        <p className="text-body-s text-red-600">{Copy.manage.dangerBody}</p>
      </div>
      <Button variant="danger" onClick={() => setConfirming(true)}>
        {Copy.manage.cancelEvent}
      </Button>
      <CancelEventDialog
        eventId={event.id}
        confirmedCount={takenSeats(event.availableSeats, event.seatLimit)}
        open={confirming}
        onClose={() => setConfirming(false)}
      />
    </section>
  );
}

function ListSkeleton() {
  return (
    <div aria-label={Copy.common.loading} className="flex flex-col gap-3">
      {Array.from({ length: LIST_SKELETON_COUNT }, (_, index) => (
        <Skeleton key={index} className="h-14 w-full" />
      ))}
    </div>
  );
}

function ManageSkeleton() {
  return (
    <div aria-label={Copy.common.loading} className="flex flex-col gap-6">
      <Skeleton className="h-10 w-2/3" />
      <Skeleton className="h-32 w-full" />
      <ListSkeleton />
    </div>
  );
}
