package com.seatly.backend.common.exception;

import com.seatly.backend.common.type.MessageKey;
import org.springframework.http.HttpStatus;

public final class UnauthorizedException extends ModuleException {

    public UnauthorizedException(MessageKey messageKey) {
        super(messageKey, HttpStatus.UNAUTHORIZED);
    }
}
