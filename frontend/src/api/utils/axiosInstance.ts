import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";
import { HttpStatus } from "@/constants/http";
import { API_BASE_PATH, ApiEndpoints } from "@/api/utils/ApiEndpoints";
import { ApiError } from "@/api/utils/ApiError";
import { isErrorEnvelope } from "@/api/utils/envelope";
import { QueryKeys } from "@/api/utils/QueryKeys";
import { getQueryClient } from "@/api/utils/queryClient";
import { RETURN_TO_PARAM, Routes } from "@/constants/routes";
import type { SessionResponse } from "@/types/responses/AuthResponse";

/**
 * The one HTTP client. Every call is same-origin to Next (/api/...):
 * there is no request interceptor adding a token, because browser code
 * can't read the httpOnly cookie. The browser sends it automatically and the
 * Next proxy route turns it into the Authorization header for Spring.
 */
export const axiosInstance = axios.create({
  baseURL: API_BASE_PATH,
  headers: { Accept: "application/json" },
});

axiosInstance.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    const apiError = toApiError(error);
    if (apiError.status === HttpStatus.UNAUTHORIZED && axios.isAxiosError(error) && error.config) {
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
  // A body that isn't our envelope (an HTML error page from a gateway, say).
  return ApiError.of(response.status, response.status >= HttpStatus.SERVER_ERROR_FLOOR ? "SERVICE_UNAVAILABLE" : "UNKNOWN_ERROR");
}

let redirectingToLogin = false;

/**
 * A 401 means "the session you had is gone" — expired or revoked token. The
 * proxy route has already cleared the cookie; here the browser side follows:
 * drop every cached response (it belonged to that user) and go to login.
 *
 * It must happen once, not per failed request and not in a loop:
 *  - Only when there WAS a session. Anonymous visitors on public pages get
 *    401s from protected calls too; bouncing them to login would be wrong.
 *  - Never for the login call itself: a wrong password is also a 401, and
 *    must show as an error on the form, not reload the page.
 *  - The flag stops parallel 401s each starting a redirect. A full page load
 *    (not client navigation) then resets everything, flag included — and
 *    wipes any in-memory state of the previous user with it.
 */
function endExpiredSession(requestConfig: InternalAxiosRequestConfig): void {
  if (requestConfig.url === ApiEndpoints.auth.login || redirectingToLogin) {
    return;
  }
  const queryClient = getQueryClient();
  const session = queryClient.getQueryData<SessionResponse>(QueryKeys.auth.session);
  if (session?.user == null) {
    return;
  }

  redirectingToLogin = true;
  queryClient.clear();
  const returnTo = `${window.location.pathname}${window.location.search}`;
  // A full page load on purpose, not router.push: it discards every piece of
  // in-memory state the previous session left behind. (This also runs outside
  // React, where there is no router to push with.)
  // eslint-disable-next-line @next/next/no-location-assign-relative-destination
  window.location.assign(`${Routes.login}?${new URLSearchParams({ [RETURN_TO_PARAM]: returnTo })}`);
}
