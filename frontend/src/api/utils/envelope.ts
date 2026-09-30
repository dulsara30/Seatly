import type { AxiosResponse } from "axios";
import type {
  ApiEnvelope,
  ApiErrorEnvelope,
} from "@/types/responses/ApiEnvelope";

// Unwrapped in the API layer, not an axios interceptor, which would make axios's types lie.
export function unwrapOne<T>(response: AxiosResponse<ApiEnvelope<T>>): T {
  const { results } = response.data;
  const [only] = results;
  if (results.length !== 1 || only === undefined) {
    throw new Error(
      `Expected exactly one result from ${response.config.url}, got ${results.length}`,
    );
  }
  return only;
}

// For endpoints where the list IS results (waitlist, my RSVPs).
export function unwrapMany<T>(response: AxiosResponse<ApiEnvelope<T>>): T[] {
  return response.data.results;
}

// Trust a body as our error envelope only if it really has that shape.
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
