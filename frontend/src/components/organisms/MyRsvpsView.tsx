"use client";

import { useState } from "react";
import { useSession } from "@/api/AuthApi";
import { useCancelRsvp, useMyRsvps } from "@/api/RsvpApi";
import { Badge } from "@/components/atoms/Badge";
import { Button, LinkButton } from "@/components/atoms/Button";
import { Skeleton } from "@/components/atoms/Skeleton";
import { EmptyState } from "@/components/molecules/EmptyState";
import { ErrorState } from "@/components/molecules/ErrorState";
import { EventListRow } from "@/components/molecules/EventListRow";
import { Tabs } from "@/components/molecules/Tabs";
import { PageHeading } from "@/components/templates/PageHeading";
import { Copy } from "@/constants/copy";
import { Routes } from "@/constants/routes";
import { LIST_SKELETON_COUNT } from "@/constants/ui";
import { ToastTone, useToastStore } from "@/store/toastStore";
import { EventStatus, RsvpStatus } from "@/types/entities/enums";
import type { MyRsvpResponse } from "@/types/responses/RsvpResponse";
import { formatLongDate, formatTime } from "@/utils/dateTime";

type Tab = typeof RsvpStatus.CONFIRMED | typeof RsvpStatus.WAITLISTED;

const TABS = [
  { id: RsvpStatus.CONFIRMED, label: Copy.myRsvps.confirmedTab },
  { id: RsvpStatus.WAITLISTED, label: Copy.myRsvps.waitlistedTab },
] as const;

const EMPTY_COPY: Record<Tab, { title: string; body: string }> = {
  CONFIRMED: {
    title: Copy.myRsvps.emptyConfirmedTitle,
    body: Copy.myRsvps.emptyConfirmedBody,
  },
  WAITLISTED: {
    title: Copy.myRsvps.emptyWaitlistedTitle,
    body: Copy.myRsvps.emptyWaitlistedBody,
  },
};

export function MyRsvpsView() {
  const [tab, setTab] = useState<Tab>(RsvpStatus.CONFIRMED);
  const session = useSession();
  // proxy.ts already guarantees a session cookie here; this waits for it to be confirmed.
  const myRsvps = useMyRsvps({ enabled: session.data?.user != null });

  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title={Copy.myRsvps.title}
        subtitle={Copy.myRsvps.subtitle}
      />
      <Tabs
        label={Copy.myRsvps.tabsLabel}
        tabs={TABS}
        active={tab}
        onChange={setTab}
      />
      {myRsvps.isPending && <ListSkeleton />}
      {myRsvps.isError && (
        <ErrorState
          title={Copy.myRsvps.errorTitle}
          message={myRsvps.error.message}
          onRetry={() => void myRsvps.refetch()}
        />
      )}
      {myRsvps.isSuccess && (
        <RsvpList
          rsvps={myRsvps.data.filter((rsvp) => rsvp.status === tab)}
          tab={tab}
        />
      )}
    </div>
  );
}

function RsvpList({ rsvps, tab }: { rsvps: MyRsvpResponse[]; tab: Tab }) {
  const cancelRsvp = useCancelRsvp();
  const showToast = useToastStore((state) => state.show);

  if (rsvps.length === 0) {
    return (
      <EmptyState
        {...EMPTY_COPY[tab]}
        action={
          <LinkButton href={Routes.home} variant="secondary" size="small">
            {Copy.myRsvps.browseEvents}
          </LinkButton>
        }
      />
    );
  }

  const cancel = (rsvp: MyRsvpResponse) =>
    cancelRsvp.mutate(rsvp.eventId, {
      onSuccess: () =>
        showToast(
          ToastTone.SUCCESS,
          rsvp.status === RsvpStatus.WAITLISTED
            ? Copy.toast.leftWaitlist
            : Copy.toast.rsvpCancelled,
        ),
      onError: (error) => showToast(ToastTone.ERROR, error.message),
    });

  return (
    <ul className="rounded-lg border border-gray-200 bg-white px-4">
      {rsvps.map((rsvp) => (
        <EventListRow
          key={rsvp.eventId}
          date={rsvp.eventDate}
          title={rsvp.eventName}
          meta={`${formatLongDate(rsvp.eventDate)} · ${formatTime(rsvp.eventDate)}`}
          href={Routes.eventDetail(rsvp.eventId)}
          trailing={
            // Not UPCOMING: offer nothing to cancel - the backend refuses RSVP changes then.
            rsvp.eventStatus !== EventStatus.UPCOMING ? (
              <Badge variant={rsvp.eventStatus} />
            ) : (
              <>
                {rsvp.status === RsvpStatus.WAITLISTED &&
                rsvp.position !== null ? (
                  <span className="text-label-m text-yellow-700">
                    {Copy.myRsvps.ahead(rsvp.position)}
                  </span>
                ) : (
                  <Badge variant="CONFIRMED" />
                )}
                <Button
                  variant="secondary"
                  size="small"
                  disabled={
                    cancelRsvp.isPending &&
                    cancelRsvp.variables === rsvp.eventId
                  }
                  onClick={() => cancel(rsvp)}>
                  {rsvp.status === RsvpStatus.WAITLISTED
                    ? Copy.detail.leaveWaitlist
                    : Copy.detail.cancelRsvp}
                </Button>
              </>
            )
          }
        />
      ))}
    </ul>
  );
}

function ListSkeleton() {
  return (
    <div aria-label={Copy.common.loading} className="flex flex-col gap-3">
      {Array.from({ length: LIST_SKELETON_COUNT }, (_, index) => (
        <Skeleton key={index} className="h-16 w-full" />
      ))}
    </div>
  );
}
