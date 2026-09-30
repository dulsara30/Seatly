import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiEndpoints } from "@/api/utils/ApiEndpoints";
import { axiosInstance } from "@/api/utils/axiosInstance";
import { unwrapMany, unwrapOne } from "@/api/utils/envelope";
import { QueryKeys } from "@/api/utils/QueryKeys";
import type { Id } from "@/types/entities/primitives";
import type { ApiEnvelope } from "@/types/responses/ApiEnvelope";
import type {
  AttendeeListResponse,
  MyRsvpResponse,
  RsvpResponse,
  WaitlistEntryResponse,
} from "@/types/responses/RsvpResponse";

const createRsvp = async (eventId: Id): Promise<RsvpResponse> =>
  unwrapOne(
    await axiosInstance.post<ApiEnvelope<RsvpResponse>>(
      ApiEndpoints.events.rsvp(eventId),
    ),
  );

const cancelRsvp = async (eventId: Id): Promise<RsvpResponse> =>
  unwrapOne(
    await axiosInstance.delete<ApiEnvelope<RsvpResponse>>(
      ApiEndpoints.events.rsvp(eventId),
    ),
  );

const fetchAttendees = async (eventId: Id): Promise<AttendeeListResponse> =>
  unwrapOne(
    await axiosInstance.get<ApiEnvelope<AttendeeListResponse>>(
      ApiEndpoints.events.attendees(eventId),
    ),
  );

// These return the list AS the envelope's results, hence unwrapMany.
const fetchWaitlist = async (eventId: Id): Promise<WaitlistEntryResponse[]> =>
  unwrapMany(
    await axiosInstance.get<ApiEnvelope<WaitlistEntryResponse>>(
      ApiEndpoints.events.waitlist(eventId),
    ),
  );

const fetchMyRsvps = async (): Promise<MyRsvpResponse[]> =>
  unwrapMany(
    await axiosInstance.get<ApiEnvelope<MyRsvpResponse>>(ApiEndpoints.rsvps.my),
  );

// An RSVP moves seat counts everywhere and can promote from the waitlist: refresh all.
const useRsvpMutation = (
  mutationFn: (eventId: Id) => Promise<RsvpResponse>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: QueryKeys.events.all });
      void queryClient.invalidateQueries({ queryKey: QueryKeys.rsvps.my });
    },
  });
};

export const useCreateRsvp = () => useRsvpMutation(createRsvp);

export const useCancelRsvp = () => useRsvpMutation(cancelRsvp);

export const useAttendees = (eventId: Id) =>
  useQuery({
    queryKey: QueryKeys.events.attendees(eventId),
    queryFn: () => fetchAttendees(eventId),
  });

export const useWaitlist = (eventId: Id) =>
  useQuery({
    queryKey: QueryKeys.events.waitlist(eventId),
    queryFn: () => fetchWaitlist(eventId),
  });

// Pass enabled: false for anonymous visitors rather than letting it 401.
export const useMyRsvps = ({ enabled }: { enabled: boolean }) =>
  useQuery({
    queryKey: QueryKeys.rsvps.my,
    queryFn: fetchMyRsvps,
    enabled,
  });
