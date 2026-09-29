package com.seatly.backend.common.exception;

import com.seatly.backend.common.type.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * 401: the caller's identity couldn't be established — bad credentials, or
 * no valid token. Distinct from ForbiddenException (403), where the caller is
 * known but not allowed.
 */
public final class UnauthorizedException extends ModuleException {

    public UnauthorizedException(MessageKey messageKey) {
        super(messageKey, HttpStatus.UNAUTHORIZED);
    }
}
