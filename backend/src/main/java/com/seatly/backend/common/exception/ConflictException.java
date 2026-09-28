package com.seatly.backend.common.exception;

import com.seatly.backend.common.type.MessageKey;
import org.springframework.http.HttpStatus;

public final class ConflictException extends ModuleException {

    public ConflictException(MessageKey messageKey) {
        super(messageKey, HttpStatus.CONFLICT);
    }
}
