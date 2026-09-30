"use client";

import Link from "next/link";
import { useSession } from "@/api/AuthApi";
import { useEvent } from "@/api/EventApi";
import { useCancelRsvp, useCreateRsvp } from "@/api/RsvpApi";
import { ApiError } from "@/api/utils/ApiError";
import { Avatar } from "@/components/atoms/Avatar";
import { Badge } from "@/components/atoms/Badge";
import { LinkButton } from "@/components/atoms/Button";
import { Icon } from "@/components/atoms/Icon";
import { Skeleton } from "@/components/atoms/Skeleton";
import { TagChip } from "@/components/atoms/TagChip";
import { EmptyState } from "@/components/molecules/EmptyState";
import { ErrorState } from "@/components/molecules/ErrorState";
import { RsvpPanel } from "@/components/molecules/RsvpPanel";
import { Copy } from "@/constants/copy";
import { HttpStatus } from "@/constants/http";
import { Routes, withReturnTo } from "@/constants/routes";
import { useMyRsvpLookup } from "@/hooks/useMyRsvpLookup";
import { ToastTone, useToastStore } from "@/store/toastStore";
import { EventMode, RsvpStatus } from "@/types/entities/enums";
import type { Id } from "@/types/entities/primitives";
import type { EventDetailResponse } from "@/types/responses/EventResponse";
import type { RsvpResponse } from "@/types/responses/RsvpResponse";
import { formatLongDate, formatTime } from "@/utils/dateTime";
import { rsvpPanelStateOf } from "@/utils/rsvpPanelState";

/** 02 / 03 / 04 Detail - one view; the RSVP panel carries the Available / Confirmed / Waitlisted states. */
export function EventDetailView({ eventId }: { eventId: Id }) {
  const event = useEvent(eventId);

  if (event.isPending) {
    return <DetailSkeleton />;
  }
  if (event.isError) {
    if (
      event.error instanceof ApiError &&
      event.error.status === HttpStatus.NOT_FOUND
    ) {
      return (
        <EmptyState
          title={Copy.detail.notFoundTitle}
          body={event.error.message}
          action={
            <LinkButton href={Routes.home} variant="secondary" size="small">
              {Copy.detail.backToEvents}
            </LinkButton>
          }
        />
      );
    }
    return (
      <ErrorState
        title={Copy.detail.errorTitle}
        message={event.error.message}
        onRetry={() => void event.refetch()}
      />
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <Link
        href={Routes.home}
        className="inline-flex w-fit items-center gap-1.5 text-label-s text-gray-500 hover:text-black">
        <Icon name="arrowLeft" />
        {Copy.detail.backToEvents}
      </Link>
      <div className="grid gap-8 lg:grid-cols-3">
        <EventSummary event={event.data} />
        <div className="lg:sticky lg:top-24 lg:self-start">
          <RsvpSection event={event.data} />
        </div>
      </div>
    </div>
  );
}

function EventSummary({ event }: { event: EventDetailResponse }) {
  const where =
    event.mode === EventMode.ONLINE ? Copy.detail.online : event.location;

  return (
    <article className="flex flex-col gap-6 lg:col-span-2">
      <div className="flex flex-col gap-3">
        <div className="flex flex-wrap gap-2">
          <Badge variant={event.status} />
          <Badge variant={event.mode} />
        </div>
        <h1 className="text-heading-xl text-black">{event.name}</h1>
      </div>

      <dl className="grid gap-4 rounded-lg bg-gray-50 p-4 sm:grid-cols-2">
        <div className="flex flex-col gap-0.5">
          <dt className="text-caption text-gray-500">
            {Copy.detail.whenLabel}
          </dt>
          <dd className="text-label-m text-black">
            {formatLongDate(event.eventDate)} · {formatTime(event.eventDate)}
          </dd>
        </div>
        <div className="flex flex-col gap-0.5">
          <dt className="text-caption text-gray-500">
            {Copy.detail.whereLabel}
          </dt>
          <dd className="text-label-m text-black">{where}</dd>
        </div>
      </dl>

      <section className="flex flex-col gap-2">
        <h2 className="text-heading-s text-black">{Copy.detail.about}</h2>
        <p className="text-body-l whitespace-pre-line text-gray-500">
          {event.description}
        </p>
      </section>

      {event.tags.length > 0 && (
        <section className="flex flex-col gap-2">
          <h2 className="text-heading-s text-black">{Copy.detail.tags}</h2>
          <ul className="flex flex-wrap gap-1.5">
            {event.tags.map((tag) => (
              <li key={tag.id}>
                <TagChip label={Copy.tag(tag.name)} />
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="flex flex-col gap-2">
        <h2 className="text-heading-s text-black">{Copy.detail.host}</h2>
        <div className="flex items-center gap-3">
          <Avatar name={event.organizer.name} size="large" />
          <span className="text-label-m text-black">
            {event.organizer.name}
          </span>
        </div>
      </section>
    </article>
  );
}

/** Owns the RSVP mutations and turns their results into toasts; the panel just renders. */
function RsvpSection({ event }: { event: EventDetailResponse }) {
  const session = useSession();
  const myRsvps = useMyRsvpLookup();
  const createRsvp = useCreateRsvp();
  const cancelRsvp = useCancelRsvp();
  const showToast = useToastStore((state) => state.show);

  // Until we know who's looking, any state shown would be a guess that flips.
  if (session.isPending || myRsvps.isLoading) {
    return <Skeleton className="h-48 w-full" />;
  }

  const state = rsvpPanelStateOf(
    event,
    session.data?.user ?? null,
    myRsvps.byEventId.get(event.id),
  );
  const showError = (error: Error) => showToast(ToastTone.ERROR, error.message);

  const onRsvp = () =>
    createRsvp.mutate(event.id, {
      onSuccess: (rsvp) => showRsvpToast(rsvp, showToast),
      onError: showError,
    });

  const onCancel = () =>
    cancelRsvp.mutate(event.id, {
      onSuccess: () =>
        showToast(
          ToastTone.SUCCESS,
          state.kind === "WAITLISTED"
            ? Copy.toast.leftWaitlist
            : Copy.toast.rsvpCancelled,
        ),
      onError: showError,
    });

  return (
    <RsvpPanel
      state={state}
      availableSeats={event.availableSeats}
      seatLimit={event.seatLimit}
      mode={event.mode}
      meetingLink={event.meetingLink}
      signInHref={withReturnTo(Routes.signIn, Routes.eventDetail(event.id))}
      manageHref={Routes.manageEvent(event.id)}
      isPending={createRsvp.isPending || cancelRsvp.isPending}
      onRsvp={onRsvp}
      onCancel={onCancel}
    />
  );
}

function showRsvpToast(
  rsvp: RsvpResponse,
  showToast: (tone: ToastTone, message: string) => void,
) {
  if (rsvp.status === RsvpStatus.WAITLISTED && rsvp.position !== null) {
    showToast(ToastTone.INFO, Copy.toast.waitlisted(rsvp.position));
    return;
  }
  showToast(ToastTone.SUCCESS, Copy.toast.confirmed);
}

function DetailSkeleton() {
  return (
    <div aria-label={Copy.common.loading} className="grid gap-8 lg:grid-cols-3">
      <div className="flex flex-col gap-4 lg:col-span-2">
        <Skeleton className="h-6 w-40" />
        <Skeleton className="h-10 w-3/4" />
        <Skeleton className="h-20 w-full" />
        <Skeleton className="h-32 w-full" />
      </div>
      <Skeleton className="h-56 w-full" />
    </div>
  );
}
