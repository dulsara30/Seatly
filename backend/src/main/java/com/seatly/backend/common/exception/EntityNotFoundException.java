package com.seatly.backend.common.exception;

import com.seatly.backend.common.type.MessageKey;
import org.springframework.http.HttpStatus;

public final class EntityNotFoundException extends ModuleException {

    public EntityNotFoundException(MessageKey messageKey) {
        super(messageKey, HttpStatus.NOT_FOUND);
    }
}
