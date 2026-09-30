package com.seatly.backend.common.exception;

import com.seatly.backend.common.type.MessageKey;
import org.springframework.http.HttpStatus;

public sealed class ModuleException extends RuntimeException
        permits EntityNotFoundException, ValidationException, ConflictException, ForbiddenException,
        UnauthorizedException {

    private final MessageKey messageKey;
    private final HttpStatus httpStatus;

    protected ModuleException(MessageKey messageKey, HttpStatus httpStatus) {
        super(messageKey.getKey());
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    public MessageKey getMessageKey() {
        return messageKey;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
