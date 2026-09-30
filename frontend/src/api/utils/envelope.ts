import type { AxiosResponse } from "axios";
import type {
  ApiEnvelope,
  ApiErrorEnvelope,
} from "@/types/responses/ApiEnvelope";

/**
 * Where the { status, results } envelope is removed - once, in the API
 * layer, so hooks and components only ever hold payload types.
 *
 * Deliberately NOT in an axios response interceptor. An interceptor that
 * swaps response.data for results[0] makes axios's own types wrong: the call
 * site would still see AxiosResponse<ApiEnvelope<Event>> while holding an
 * Event. Here the types change exactly where the value changes.
 *
 * The backend uses the envelope two ways, so there are two unwrappers:
 *  - one object in results (most endpoints)
 *  - the list IS results (waitlist, my RSVPs)
 */

export function unwrapOne<T>(response: AxiosResponse<ApiEnvelope<T>>): T {
  const { results } = response.data;
  const [only] = results;
  // A contract violation, not a user error - surface it loudly.
  if (results.length !== 1 || only === undefined) {
    throw new Error(
      `Expected exactly one result from ${response.config.url}, got ${results.length}`,
    );
  }
  return only;
}

export function unwrapMany<T>(response: AxiosResponse<ApiEnvelope<T>>): T[] {
  return response.data.results;
}

/** An error body is only trusted as our envelope if it actually has the shape. */
export function isErrorEnvelope(body: unknown): body is ApiErrorEnvelope {
  return (
    typeof body === "object" &&
    body !== null &&
    "status" in body &&
    body.status === "unsuccessful" &&
    "results" in body &&
    Array.isArray(body.results)
  );
}
