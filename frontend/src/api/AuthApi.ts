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

// Server state: kept in this query, not Zustand, so there's one source of truth.
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
      // Event responses depend on who's asking (meetingLink), so logged-out fetches are stale.
      void queryClient.invalidateQueries({ queryKey: QueryKeys.events.all });
    },
  });
};

export const useLogout = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: logout,
    onSuccess: (session) => {
      queryClient.clear();
      queryClient.setQueryData(QueryKeys.auth.session, session);
    },
  });
};

// Registration returns no token, so this does not log in.
export const useRegister = () => useMutation({ mutationFn: register });
