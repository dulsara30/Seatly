import { MESSAGES, isMessageKey, type MessageKey } from "@/constants/messages";
import type { ApiErrorEnvelope } from "@/types/responses/ApiEnvelope";

export interface ApiErrorDetail {
  key: MessageKey;
  message: string;
  field: string | null;
}

export class ApiError extends Error {
  // null when the request never got an HTTP response (network failure).
  readonly status: number | null;
  readonly details: readonly [ApiErrorDetail, ...ApiErrorDetail[]];

  private constructor(
    status: number | null,
    details: readonly [ApiErrorDetail, ...ApiErrorDetail[]],
  ) {
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
    if (first === undefined) {
      return ApiError.of(status, "UNKNOWN_ERROR");
    }
    return new ApiError(status, [first, ...rest]);
  }

  static of(status: number | null, key: MessageKey): ApiError {
    return new ApiError(status, [detail(key, null)]);
  }
}

// A newer backend can send a key this build doesn't know: map it to UNKNOWN_ERROR.
function detail(rawKey: string, field: string | null): ApiErrorDetail {
  if (isMessageKey(rawKey)) {
    return { key: rawKey, message: MESSAGES[rawKey], field };
  }
  if (process.env.NODE_ENV === "development") {
    console.warn(
      `No copy for backend message key "${rawKey}" - add it to constants/messages.ts`,
    );
  }
  return { key: "UNKNOWN_ERROR", message: MESSAGES.UNKNOWN_ERROR, field };
}
