import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiEndpoints } from "@/api/utils/ApiEndpoints";
import { axiosInstance } from "@/api/utils/axiosInstance";
import { unwrapOne } from "@/api/utils/envelope";
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
import type { EventDetailResponse, EventResponse } from "@/types/responses/EventResponse";
import type { PagedResponse } from "@/types/responses/PagedResponse";

const fetchUpcomingEvents = async (params: EventListParams): Promise<PagedResponse<EventResponse>> =>
  unwrapOne(await axiosInstance.get<ApiEnvelope<PagedResponse<EventResponse>>>(ApiEndpoints.events.list, { params }));

const fetchEvent = async (eventId: Id): Promise<EventDetailResponse> =>
  unwrapOne(await axiosInstance.get<ApiEnvelope<EventDetailResponse>>(ApiEndpoints.events.detail(eventId)));

const createEvent = async (request: CreateEventRequest): Promise<EventDetailResponse> =>
  unwrapOne(await axiosInstance.post<ApiEnvelope<EventDetailResponse>>(ApiEndpoints.events.list, request));

const updateEvent = async ({ eventId, request }: { eventId: Id; request: UpdateEventRequest }) =>
  unwrapOne(await axiosInstance.patch<ApiEnvelope<EventDetailResponse>>(ApiEndpoints.events.detail(eventId), request));

/** The browse grid: infinite scroll over GET /v1/events, soonest first. */
export const useUpcomingEvents = (filters: EventFilters) => {
  const params = { ...filters, size: DEFAULT_PAGE_SIZE };
  return useInfiniteQuery({
    queryKey: QueryKeys.events.list(params),
    queryFn: ({ pageParam }) => fetchUpcomingEvents({ ...params, page: pageParam }),
    initialPageParam: FIRST_PAGE,
    // currentPage is zero-based; undefined tells TanStack there are no more pages.
    getNextPageParam: (lastPage) =>
      lastPage.currentPage + 1 < lastPage.totalPages ? lastPage.currentPage + 1 : undefined,
  });
};

export const useEvent = (eventId: Id) =>
  useQuery({
    queryKey: QueryKeys.events.detail(eventId),
    queryFn: () => fetchEvent(eventId),
  });

export const useCreateEvent = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createEvent,
    onSuccess: (event) => {
      queryClient.setQueryData(QueryKeys.events.detail(event.id), event);
      void queryClient.invalidateQueries({ queryKey: QueryKeys.events.lists });
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
      // attendee and waitlist views under this event are stale too — and so
      // is "my RSVPs" for anyone looking at their own promotion.
      void queryClient.invalidateQueries({ queryKey: QueryKeys.events.all });
      void queryClient.invalidateQueries({ queryKey: QueryKeys.rsvps.my });
    },
  });
};
