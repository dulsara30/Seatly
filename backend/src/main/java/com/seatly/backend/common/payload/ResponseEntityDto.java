package com.seatly.backend.common.payload;

import com.seatly.backend.common.type.MessageKey;
import com.seatly.backend.common.type.ResponseStatusType;
import java.util.List;

public record ResponseEntityDto<T>(ResponseStatusType status, List<T> results) {

    public static <T> ResponseEntityDto<T> success(T result) {
        return new ResponseEntityDto<>(ResponseStatusType.SUCCESSFUL, List.of(result));
    }

    public static <T> ResponseEntityDto<T> success(List<T> results) {
        return new ResponseEntityDto<>(ResponseStatusType.SUCCESSFUL, results);
    }

    public static ResponseEntityDto<ErrorMessage> error(MessageKey messageKey) {
        return new ResponseEntityDto<>(ResponseStatusType.UNSUCCESSFUL, List.of(new ErrorMessage(messageKey.getKey())));
    }

    public static ResponseEntityDto<FieldValidationError> validationError(List<FieldValidationError> errors) {
        return new ResponseEntityDto<>(ResponseStatusType.UNSUCCESSFUL, errors);
    }

    public record ErrorMessage(String message) {
    }

    /**
     * message is always CommonMessageKey.VALIDATION_FAILED — a stable key,
     * per the same contract every other error uses. field/detail carry the
     * specifics, which come from Bean Validation and are prose until a
     * DTO's annotation sets its own key-shaped message.
     */
    public record FieldValidationError(String field, String message, String detail) {
    }
}
