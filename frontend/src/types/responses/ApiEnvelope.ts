// common/payload/ResponseEntityDto.java; results is always an array, even for one object.

// common/type/ResponseStatusType.java, serialised by its @JsonValue
export type ApiStatus = "successful" | "unsuccessful";

export interface ApiEnvelope<T> {
  status: "successful";
  results: T[];
}

// ResponseEntityDto.ErrorMessage - `message` is a message KEY, not prose.
export interface ApiErrorMessage {
  message: string;
}

// ResponseEntityDto.FieldValidationError - Bean Validation on a request field.
export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiErrorEnvelope {
  status: "unsuccessful";
  results: Array<ApiErrorMessage | ApiFieldError>;
}
