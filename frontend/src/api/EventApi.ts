import {
  type InfiniteData,
  useInfiniteQuery,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { useCallback } from "react";
import { ApiEndpoints } from "@/api/utils/ApiEndpoints";
import { axiosInstance } from "@/api/utils/axiosInstance";
import { unwrapMany, unwrapOne } from "@/api/utils/envelope";
import { QueryKeys } from "@/api/utils/QueryKeys";
import { DEFAULT_PAGE_SIZE, FIRST_PAGE } from "@/constants/pagination";
import type { Id } from "@/types/entities/primitives";
import type {
  CreateEventRequest,
  EventFilters,
  EventListParams,
  UpdateEventRequest,
} from "@/types/requests/EventRequests";
import type { ApiEnvelope } from "@/types/responses/ApiEnvelope";
import type {
  EventDetailResponse,
  EventResponse,
} from "@/types/responses/EventResponse";
import type { PagedResponse } from "@/types/responses/PagedResponse";
import type { SeatCountUpdate } from "@/types/responses/SeatCountUpdate";

const fetchUpcomingEvents = async (
  params: EventListParams,
): Promise<PagedResponse<EventResponse>> =>
  unwrapOne(
    await axiosInstance.get<ApiEnvelope<PagedResponse<EventResponse>>>(
      ApiEndpoints.events.list,
      { params },
    ),
  );

const fetchEvent = async (eventId: Id): Promise<EventDetailResponse> =>
  unwrapOne(
    await axiosInstance.get<ApiEnvelope<EventDetailResponse>>(
      ApiEndpoints.events.detail(eventId),
    ),
  );

const createEvent = async (
  request: CreateEventRequest,
): Promise<EventDetailResponse> =>
  unwrapOne(
    await axiosInstance.post<ApiEnvelope<EventDetailResponse>>(
      ApiEndpoints.events.list,
      request,
    ),
  );

const updateEvent = async ({
  eventId,
  request,
}: {
  eventId: Id;
  request: UpdateEventRequest;
}) =>
  unwrapOne(
    await axiosInstance.patch<ApiEnvelope<EventDetailResponse>>(
      ApiEndpoints.events.detail(eventId),
      request,
    ),
  );

const fetchMyEvents = async (): Promise<EventResponse[]> =>
  unwrapMany(
    await axiosInstance.get<ApiEnvelope<EventResponse>>(ApiEndpoints.events.my),
  );

const cancelEvent = async (eventId: Id): Promise<EventDetailResponse> =>
  unwrapOne(
    await axiosInstance.post<ApiEnvelope<EventDetailResponse>>(
      ApiEndpoints.events.cancel(eventId),
    ),
  );

/** The browse grid: infinite scroll over GET /v1/events, soonest first. */
export const useUpcomingEvents = (filters: EventFilters) => {
  const params = { ...filters, size: DEFAULT_PAGE_SIZE };
  return useInfiniteQuery({
    queryKey: QueryKeys.events.list(params),
    queryFn: ({ pageParam }) =>
      fetchUpcomingEvents({ ...params, page: pageParam }),
    initialPageParam: FIRST_PAGE,
    // currentPage is zero-based; undefined tells TanStack there are no more pages.
    getNextPageParam: (lastPage) =>
      lastPage.currentPage + 1 < lastPage.totalPages
        ? lastPage.currentPage + 1
        : undefined,
  });
};

export const useEvent = (eventId: Id) =>
  useQuery({
    queryKey: QueryKeys.events.detail(eventId),
    queryFn: () => fetchEvent(eventId),
  });

/** The organiser's own events, every status - the dashboard. */
export const useMyEvents = () =>
  useQuery({
    queryKey: QueryKeys.events.mine,
    queryFn: fetchMyEvents,
  });

export const useCreateEvent = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createEvent,
    onSuccess: (event) => {
      queryClient.setQueryData(QueryKeys.events.detail(event.id), event);
      void queryClient.invalidateQueries({ queryKey: QueryKeys.events.lists });
      void queryClient.invalidateQueries({ queryKey: QueryKeys.events.mine });
    },
  });
};

/** Cancelling removes the event from every list and ends every attendee's RSVP. */
export const useCancelEvent = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: cancelEvent,
    onSuccess: (event) => {
      queryClient.setQueryData(QueryKeys.events.detail(event.id), event);
      void queryClient.invalidateQueries({ queryKey: QueryKeys.events.all });
      void queryClient.invalidateQueries({ queryKey: QueryKeys.rsvps.my });
    },
  });
};

export const useUpdateEvent = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateEvent,
    onSuccess: (event) => {
      queryClient.setQueryData(QueryKeys.events.detail(event.id), event);
      // Raising the seat limit promotes people off the waitlist, so the
      // attendee and waitlist views under this event are stale too - and so
      // is "my RSVPs" for anyone looking at their own promotion.
      void queryClient.invalidateQueries({ queryKey: QueryKeys.events.all });
      void queryClient.invalidateQueries({ queryKey: QueryKeys.rsvps.my });
    },
  });
};

/** The event with a live update's counts applied. seatLimit moves too when the organiser raised it. */
function withSeatCounts<Event extends EventResponse>(event: Event, update: SeatCountUpdate): Event {
  return {
    ...event,
    availableSeats: update.availableSeats,
    seatLimit: update.availableSeats + update.confirmedCount,
  };
}

/**
 * Applies a pushed seat count to every cached copy of that event — its
 * detail, every loaded browse page, the organiser's list — with setQueryData,
 * never a refetch: the push already IS the new value, so asking the server
 * again would be a wasted request per viewer per change.
 *
 * Returns a stable function, so the stream hook can hold on to it.
 */
export const useApplySeatUpdate = () => {
  const queryClient = useQueryClient();

  return useCallback(
    (update: SeatCountUpdate) => {
      const applyIfThisEvent = <Event extends EventResponse>(event: Event) =>
        event.id === update.eventId ? withSeatCounts(event, update) : event;

      // Each updater returns undefined when nothing is cached, which leaves it
      // uncached — a push doesn't create data for a page nobody has opened.
      queryClient.setQueryData<EventDetailResponse>(QueryKeys.events.detail(update.eventId), (event) =>
        event ? withSeatCounts(event, update) : event,
      );
      queryClient.setQueriesData<InfiniteData<PagedResponse<EventResponse>>>(
        { queryKey: QueryKeys.events.lists },
        (data) =>
          data && {
            ...data,
            pages: data.pages.map((page) => ({ ...page, items: page.items.map(applyIfThisEvent) })),
          },
      );
      queryClient.setQueryData<EventResponse[]>(QueryKeys.events.mine, (events) => events?.map(applyIfThisEvent));
    },
    [queryClient],
  );
};
