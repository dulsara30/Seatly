package com.seatly.backend.common.exception;

import com.seatly.backend.common.type.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Base of every business-rule failure a service throws on purpose. Sealed to
 * the five HTTP-meaning subtypes below — a module never throws this
 * directly, it picks the subtype that matches the status the caller should
 * see, and supplies its own MessageKey.
 */
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
