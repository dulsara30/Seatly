import { MESSAGES, isMessageKey, type MessageKey } from "@/constants/messages";
import type { ApiErrorEnvelope } from "@/types/responses/ApiEnvelope";

export interface ApiErrorDetail {
  key: MessageKey;
  /** The user-facing copy for `key`, from constants/messages.ts. */
  message: string;
  /** Set for Bean Validation failures — which request field was rejected. */
  field: string | null;
}

/**
 * The one error type every API call rejects with. Components and forms read
 * `message` (or `details` for per-field errors) and never touch axios errors
 * or raw backend keys.
 */
export class ApiError extends Error {
  /** null when the request never got an HTTP response (network failure). */
  readonly status: number | null;
  /** Never empty — the type guarantees there is always a first reason. */
  readonly details: readonly [ApiErrorDetail, ...ApiErrorDetail[]];

  private constructor(status: number | null, details: readonly [ApiErrorDetail, ...ApiErrorDetail[]]) {
    super(details.map((errorDetail) => errorDetail.message).join(" "));
    this.name = "ApiError";
    this.status = status;
    this.details = details;
  }

  get key(): MessageKey {
    return this.details[0].key;
  }

  static fromEnvelope(status: number, envelope: ApiErrorEnvelope): ApiError {
    const [first, ...rest] = envelope.results.map((result) =>
      detail(result.message, "field" in result ? result.field : null),
    );
    // The backend always sends at least one reason; an empty list would be
    // its bug, and "unknown error" is then the honest description.
    if (first === undefined) {
      return ApiError.of(status, "UNKNOWN_ERROR");
    }
    return new ApiError(status, [first, ...rest]);
  }

  static of(status: number | null, key: MessageKey): ApiError {
    return new ApiError(status, [detail(key, null)]);
  }
}

/**
 * The backend is the source of these keys, and a newer backend can send one
 * this build doesn't know yet. That's a real boundary, so an unknown key
 * becomes UNKNOWN_ERROR — and is logged in development so the missing entry
 * in messages.ts gets noticed rather than hidden.
 */
function detail(rawKey: string, field: string | null): ApiErrorDetail {
  if (isMessageKey(rawKey)) {
    return { key: rawKey, message: MESSAGES[rawKey], field };
  }
  if (process.env.NODE_ENV === "development") {
    console.warn(`No copy for backend message key "${rawKey}" — add it to constants/messages.ts`);
  }
  return { key: "UNKNOWN_ERROR", message: MESSAGES.UNKNOWN_ERROR, field };
}
