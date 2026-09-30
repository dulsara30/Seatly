import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";
import { HttpStatus } from "@/constants/http";
import { API_BASE_PATH, ApiEndpoints } from "@/api/utils/ApiEndpoints";
import { ApiError } from "@/api/utils/ApiError";
import { isErrorEnvelope } from "@/api/utils/envelope";
import { QueryKeys } from "@/api/utils/QueryKeys";
import { getQueryClient } from "@/api/utils/queryClient";
import { RETURN_TO_PARAM, Routes } from "@/constants/routes";
import type { SessionResponse } from "@/types/responses/AuthResponse";

// No request interceptor: JS can't read the httpOnly cookie; the proxy adds the Bearer header.
export const axiosInstance = axios.create({
  baseURL: API_BASE_PATH,
  headers: { Accept: "application/json" },
});

axiosInstance.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    const apiError = toApiError(error);
    if (
      apiError.status === HttpStatus.UNAUTHORIZED &&
      axios.isAxiosError(error) &&
      error.config
    ) {
      endExpiredSession(error.config);
    }
    return Promise.reject(apiError);
  },
);

function toApiError(error: unknown): ApiError {
  if (!axios.isAxiosError(error)) {
    return ApiError.of(null, "UNKNOWN_ERROR");
  }
  const { response } = error as AxiosError<unknown>;
  if (!response) {
    return ApiError.of(null, "NETWORK_ERROR");
  }
  if (isErrorEnvelope(response.data)) {
    return ApiError.fromEnvelope(response.status, response.data);
  }
  return ApiError.of(
    response.status,
    response.status >= HttpStatus.SERVER_ERROR_FLOOR
      ? "SERVICE_UNAVAILABLE"
      : "UNKNOWN_ERROR",
  );
}

let redirectingToLogin = false;

// Runs once, only if a session existed, never for the login call (a wrong password is a 401).
function endExpiredSession(requestConfig: InternalAxiosRequestConfig): void {
  if (requestConfig.url === ApiEndpoints.auth.login || redirectingToLogin) {
    return;
  }
  const queryClient = getQueryClient();
  const session = queryClient.getQueryData<SessionResponse>(
    QueryKeys.auth.session,
  );
  if (session?.user == null) {
    return;
  }

  redirectingToLogin = true;
  queryClient.clear();
  const returnTo = `${window.location.pathname}${window.location.search}`;
  // Full page load, not router.push, so the previous user's in-memory state is wiped.
  // eslint-disable-next-line @next/next/no-location-assign-relative-destination
  window.location.assign(
    `${Routes.signIn}?${new URLSearchParams({ [RETURN_TO_PARAM]: returnTo })}`,
  );
}
