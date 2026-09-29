/**
 * common/payload/ResponseEntityDto.java — every backend response, success or
 * failure, arrives in this envelope.
 *
 * `results` is always an array. Most endpoints put one object in it; list
 * endpoints that aren't paged (waitlist, my RSVPs) put the list itself in it.
 * The API layer unwraps the envelope (api/utils/envelope.ts), so hooks and
 * components only ever see the payload type — never this.
 */

// common/type/ResponseStatusType.java, serialised by its @JsonValue
export type ApiStatus = "successful" | "unsuccessful";

export interface ApiEnvelope<T> {
  status: "successful";
  results: T[];
}

// ResponseEntityDto.ErrorMessage — `message` is a message KEY, not prose.
export interface ApiErrorMessage {
  message: string;
}

// ResponseEntityDto.FieldValidationError — Bean Validation on a request field.
export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiErrorEnvelope {
  status: "unsuccessful";
  results: Array<ApiErrorMessage | ApiFieldError>;
}
