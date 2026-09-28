package com.seatly.backend.common.exception;

import com.seatly.backend.common.type.MessageKey;
import org.springframework.http.HttpStatus;

public final class ForbiddenException extends ModuleException {

    public ForbiddenException(MessageKey messageKey) {
        super(messageKey, HttpStatus.FORBIDDEN);
    }
}
