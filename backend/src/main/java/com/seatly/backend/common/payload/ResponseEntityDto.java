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

    /**
     * For error sources that already produce key-shaped strings themselves
     * (e.g. a @Min/@Max message attribute set to a CommonMessageKeys
     * constant), rather than a single MessageKey enum value.
     */
    public static ResponseEntityDto<ErrorMessage> errors(List<String> keys) {
        return new ResponseEntityDto<>(ResponseStatusType.UNSUCCESSFUL, keys.stream().map(ErrorMessage::new).toList());
    }

    public static ResponseEntityDto<FieldValidationError> validationError(List<FieldValidationError> errors) {
        return new ResponseEntityDto<>(ResponseStatusType.UNSUCCESSFUL, errors);
    }

    public record ErrorMessage(String message) {
    }

    /**
     * message is the DTO's own annotation-supplied key (e.g.
     * @NotBlank(message = EventMessageKeys.NAME_REQUIRED)) — every request
     * DTO's Bean Validation annotations set one explicitly, so this is
     * already key-shaped, not prose. field says which one failed.
     */
    public record FieldValidationError(String field, String message) {
    }
}
