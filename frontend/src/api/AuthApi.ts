import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiEndpoints } from "@/api/utils/ApiEndpoints";
import { axiosInstance } from "@/api/utils/axiosInstance";
import { unwrapOne } from "@/api/utils/envelope";
import { QueryKeys } from "@/api/utils/QueryKeys";
import type { User } from "@/types/entities/User";
import type {
  LoginRequest,
  RegisterRequest,
} from "@/types/requests/AuthRequests";
import type { ApiEnvelope } from "@/types/responses/ApiEnvelope";
import type { SessionResponse } from "@/types/responses/AuthResponse";

const fetchSession = async (): Promise<SessionResponse> =>
  unwrapOne(
    await axiosInstance.get<ApiEnvelope<SessionResponse>>(
      ApiEndpoints.auth.session,
    ),
  );

const login = async (request: LoginRequest): Promise<SessionResponse> =>
  unwrapOne(
    await axiosInstance.post<ApiEnvelope<SessionResponse>>(
      ApiEndpoints.auth.login,
      request,
    ),
  );

const logout = async (): Promise<SessionResponse> =>
  unwrapOne(
    await axiosInstance.post<ApiEnvelope<SessionResponse>>(
      ApiEndpoints.auth.logout,
    ),
  );

const register = async (request: RegisterRequest): Promise<User> =>
  unwrapOne(
    await axiosInstance.post<ApiEnvelope<User>>(
      ApiEndpoints.auth.register,
      request,
    ),
  );

/**
 * Who is logged in - the single source of truth for the current user. The
 * user is server state, so it lives in this query, not in a Zustand store:
 * a copy there would be a second source of truth that goes stale.
 */
export const useSession = () =>
  useQuery({
    queryKey: QueryKeys.auth.session,
    queryFn: fetchSession,
  });

export const useLogin = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: login,
    onSuccess: (session) => {
      queryClient.setQueryData(QueryKeys.auth.session, session);
      // What an event shows depends on who is asking (meetingLink), so
      // anything fetched while logged out is now out of date.
      void queryClient.invalidateQueries({ queryKey: QueryKeys.events.all });
    },
  });
};

export const useLogout = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: logout,
    onSuccess: (session) => {
      // Everything cached belonged to the user who just left.
      queryClient.clear();
      queryClient.setQueryData(QueryKeys.auth.session, session);
    },
  });
};

/** Creates the account only - it does not log in. Registration returns no token. */
export const useRegister = () => useMutation({ mutationFn: register });
