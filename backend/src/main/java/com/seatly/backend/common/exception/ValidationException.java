package com.seatly.backend.common.exception;

import com.seatly.backend.common.type.MessageKey;
import org.springframework.http.HttpStatus;

public final class ValidationException extends ModuleException {

    public ValidationException(MessageKey messageKey) {
        super(messageKey, HttpStatus.BAD_REQUEST);
    }
}
